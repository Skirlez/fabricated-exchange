package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.BrewingRecipeAccessor;
import com.skirlez.fabricatedexchange.mixin.BrewingRecipeRegistryAccessor;
import com.skirlez.fabricatedexchange.mixin.LegacySmithingRecipeAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModConfig;
import com.skirlez.fabricatedexchange.util.config.ModifiersFile;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;


// This class tries to infer EMC values of items through in-game recipes.

// In areas of code where a possible rare case is not considered, 
// a comment prefixed with "UNCONSIDERED:" should be left
public class EmcMapper {
    private ConcurrentMap<String, SuperNumber> emcMap;
    private ConcurrentMap<String, SuperNumber> potionEmcMap;

    private boolean warninged = false;
    
    private RecipeManager recipeManager;
    private Map<String, Set<UnknownEquationNode>> unknownEquationMap = new HashMap<String, Set<UnknownEquationNode>>();
    
    private HashMap<String, Queue<ItemEquation>> splitQueues = new HashMap<String, Queue<ItemEquation>>();
    private final ModifiersFile modifiers;

    public EmcMapper(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;

        emcMap = new ConcurrentHashMap<String, SuperNumber>();
        potionEmcMap = new ConcurrentHashMap<String, SuperNumber>();

        modifiers = ModConfig.MODIFIERS;
    }

    public ConcurrentMap<String, SuperNumber> getEmcMap() {
        return emcMap;
    }
    public ConcurrentMap<String, SuperNumber> getPotionEmcMap() {
        return potionEmcMap;
    }


    /** Maps out all the recipes known to recipeManager + item equations given by mods. */
    public boolean map() {

        Map<String, SuperNumber> seedEmcMap = ModConfig.SEED_EMC_MAP_FILE.getValue();
        if (seedEmcMap != null)
            GeneralUtil.mergeMap(emcMap, seedEmcMap);

        List<SmithingRecipe> allSmithingRecipes = recipeManager.listAllOfType(RecipeType.SMITHING);
        List<SmeltingRecipe> allSmeltingRecipes = recipeManager.listAllOfType(RecipeType.SMELTING);
        List<CraftingRecipe> allCraftingRecipes = recipeManager.listAllOfType(RecipeType.CRAFTING);
        // blacklisted recipes and items
        Map<String, HashSet<String>> blacklistedRecipes = ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.getValue();
        if (blacklistedRecipes == null)
            blacklistedRecipes = new HashMap<>();
        HashSet<String> smithingRecipesBlacklist = blacklistedRecipes.getOrDefault("smithing", new HashSet<String>());
        HashSet<String> smeltingRecipesBlacklist = blacklistedRecipes.getOrDefault("smelting", new HashSet<String>());
        HashSet<String> craftingRecipesBlacklist = blacklistedRecipes.getOrDefault("crafting", new HashSet<String>());
        
        // we must split the item equations between their origin mod. 
        // minecraft item equations should evaluate first, and then we don't actually care about the order.
        HashSet<String> namespaces = new HashSet<String>();

        namespaces.add("minecraft");

        splitRecipesToMods(allCraftingRecipes, namespaces, craftingRecipesBlacklist, this::createEquation);
        splitRecipesToMods(allSmithingRecipes, namespaces, smithingRecipesBlacklist, this::createEquation);
        splitRecipesToMods(allSmeltingRecipes, namespaces, smeltingRecipesBlacklist, this::createEquation);


        // potion recipes are special recipes, and don't inherit from the Recipe interface. So we treat them in a special way.
        List<BrewingRecipeRegistry.Recipe<Item>> itemPotionRecipes = BrewingRecipeRegistryAccessor.getItemRecipes();
        for (BrewingRecipeRegistry.Recipe<Item> recipe : itemPotionRecipes) {
            BrewingRecipeAccessor<Item> recipeAccessor = (BrewingRecipeAccessor<Item>)recipe;
            Item input = recipeAccessor.getInput();
            Ingredient ingredient = recipeAccessor.getIngredient();
            Item output = recipeAccessor.getOutput();

            DefaultedList<Ingredient> ingredientList = DefaultedList.of();
            ingredientList.add(ingredient);
            ingredientList.add(Ingredient.ofItems(input));
            ingredientList.add(Ingredient.ofItems(input));
            ingredientList.add(Ingredient.ofItems(input));

            String name = "minecraft:" + Registry.ITEM.getId(input).getPath() + "_to_" + 
                Registry.ITEM.getId(output).getPath() + "_using_" + 
                Registry.ITEM.getId(ingredient.getMatchingStacks()[0].getItem()).getPath();

            ItemEquation equation = new ItemEquation(
                ingredientList, 
                Collections.singletonList(new ItemStack(output, 3)), 
                new Identifier(name));
                
            countUnknowns(equation);
        }

        // TODO: inject recipes provided by mods here

        namespaces.remove("minecraft");
        List<String> mods = new ArrayList<String>(namespaces.size() + 1);
        mods.add("minecraft");
        mods.addAll(namespaces);
        

        for (int m = 0; m < mods.size(); m++) {
            String mod = mods.get(m);
            Queue<ItemEquation> queue = splitQueues.get(mod);
            if (queue == null)
                continue;
            while (!queue.isEmpty()) {
                ItemEquation equation = queue.poll();
                if (equation.amountUnknown != 1)
                    continue;

                solve(equation);
            }
        }


        // most potion recipes are very very special, so we need to take care of them separately.
        // i've decided to just copy this section from the old emc mapper, using the dumber method, 
        // because there are not that many potion recipes.
        potionEmcMap = new ConcurrentHashMap<String, SuperNumber>();
        potionEmcMap.put("minecraft:water", new SuperNumber(0));
        List<BrewingRecipeRegistry.Recipe<Potion>> potionRecipes = BrewingRecipeRegistryAccessor.getPotionRecipes();
        for (int i = 0; i < 100; i++) {
            boolean newInfo = false;
            for (BrewingRecipeRegistry.Recipe<Potion> recipe : potionRecipes)
                newInfo = iteratePotionRecipe((BrewingRecipeAccessor<Potion>)recipe) || newInfo;
            if (!newInfo)
                break;
        }

        Map<String, SuperNumber> customEmcMap = ModConfig.CUSTOM_EMC_MAP_FILE.getValue();
        if (customEmcMap != null)
            GeneralUtil.mergeMap(emcMap, customEmcMap);

        boolean result = warninged;
        warninged = false;
        return result;
        // we're done!!!!!!!!!!!!!
    }


    private ItemEquation createEquation(CraftingRecipe recipe) {
        List<Ingredient> ingredients = recipe.getIngredients();
        List<ItemStack> output = new ArrayList<ItemStack>(1);
        output.add(recipe.getOutput());
        List<Ingredient> filteredIngredients = new ArrayList<Ingredient>(ingredients.size());

        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            // UNCONSIDERED: ingredients with itemstacks that have different recipe remainders
            // UNCONSIDERED: ingredients with some itemstacks being modifiers and some not
            // UNCONSIDERED: ingredients with multiple itemstacks having different recipe remainders
            ItemStack addRemainder = null;
            boolean add = false;
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (stack.isEmpty())
                    break;
                if (modifiers.hasItem(Registry.ITEM.getId(stack.getItem()).toString()) && ingredients.size() != 1)
                    break;

                ItemStack remainder = stack.getRecipeRemainder();
                if (!remainder.isEmpty()) {
                    if (!ItemStack.areEqual(stack, remainder)) {
                        addRemainder = remainder;
                        add = true;
                    }
                }
                else
                    add = true;
            }
            if (addRemainder != null)
                output.add(addRemainder);
            if (add)
                filteredIngredients.add(ingredient);
        }
        return new ItemEquation(filteredIngredients,
            Collections.singletonList(recipe.getOutput()),
            recipe.getId());
    }

    private ItemEquation createEquation(SmeltingRecipe recipe) {
        return new ItemEquation(
            Collections.singletonList(recipe.getIngredients().get(0)),
            Collections.singletonList(recipe.getOutput()),
            recipe.getId());
    }


    @Nullable
    private ItemEquation createEquation(SmithingRecipe recipe) {
        if (!(recipe instanceof SmithingRecipe))
            return null;

        LegacySmithingRecipeAccessor recipeAccessor = (LegacySmithingRecipeAccessor) recipe;
        List<Ingredient> ingredients = new ArrayList<Ingredient>();
        ingredients.add(recipeAccessor.getBase());
        ingredients.add(recipeAccessor.getAddition());

        return new ItemEquation(
            ingredients,
            Collections.singletonList(recipe.getOutput()),
            recipe.getId());
    }



    private <T extends Recipe<?>> void splitRecipesToMods(List<T> allRecipes, HashSet<String> namespaces, 
            HashSet<String> blacklist, Function<T, ItemEquation> equationConvertion) {
        for (int i = 0; i < allRecipes.size(); i++) {
            T recipe = allRecipes.get(i);
            if (blacklist.contains(recipe.getId().toString()))
                continue;
            String namespace = recipe.getId().getNamespace();
            ItemEquation equation = equationConvertion.apply(recipe);
            if (equation == null)
                continue;
            if (!namespaces.contains(namespace))
                namespaces.add(namespace);
            countUnknowns(equation);
        }
    }


    private boolean emcMapHasEntry(Item item) {
        return emcMap.containsKey(itemName(item));
    }


    private String itemName(Item item) {
        return Registry.ITEM.getId(item).toString();
    }

    private void registerUnknownEquation(String itemId, ItemEquation equation) {
        UnknownEquationNode node = new UnknownEquationNode(equation);
        if (unknownEquationMap.containsKey(itemId))
            unknownEquationMap.get(itemId).add(node);
        else {
            Set<UnknownEquationNode> set = new HashSet<UnknownEquationNode>();
            set.add(node);
            unknownEquationMap.put(itemId, set);
        }
    }
    private void registerUnknownEquation(Ingredient ingredient, ItemEquation equation) {
        UnknownEquationNode node = new UnknownEquationNode(equation);
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            String itemId = itemName(stack.getItem());
            if (unknownEquationMap.containsKey(itemId))
                unknownEquationMap.get(itemId).add(node);
            else {
                Set<UnknownEquationNode> set = new HashSet<UnknownEquationNode>();
                set.add(node);
                unknownEquationMap.put(itemId, set);
            }
        }
    }



    public int countUnknowns(ItemEquation equation) {
        int amountUnknown = 0;
        Set<String> unknownItems = new HashSet<String>();
        Set<Ingredient> unknownIngredients = new HashSet<Ingredient>();
        for (Ingredient ingredient : equation.input) {
            SuperNumber value = getIngredientEmc(ingredient, equation.name);
            if (value.equalsZero()) {
                boolean contains = false;
                for (Ingredient ingredient2 : unknownIngredients) {
                    if (Arrays.equals(ingredient.getMatchingStacks(), ingredient2.getMatchingStacks())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    unknownIngredients.add(ingredient);
                    amountUnknown++;
                }

            }
        }
        for (ItemStack stack : equation.output) {
            if (!emcMapHasEntry(stack.getItem())) {
                unknownItems.add(itemName(stack.getItem()));
                amountUnknown++;
            }
        }

        if (amountUnknown > 0) {
            if (amountUnknown == 1) {
                if (splitQueues.containsKey(equation.origin))
                    splitQueues.get(equation.origin).add(equation);
                else {
                    Queue<ItemEquation> modQueue = new LinkedList<ItemEquation>();
                    modQueue.add(equation);
                    splitQueues.put(equation.origin, modQueue);
                }
            }
            for (String item : unknownItems)
                registerUnknownEquation(item, equation);
            for (Ingredient ingredient : unknownIngredients)
                registerUnknownEquation(ingredient, equation);
        }

        equation.amountUnknown = amountUnknown;
        return amountUnknown;

    }

    private void solve(ItemEquation equation) {
        SuperNumber sum = SuperNumber.Zero();

        HashSet<Item> unknownItems = new HashSet<Item>();
        int unknownMult = 0;

  
        for (ItemStack stack : equation.output) {
            SuperNumber emc = getItemStackEmc(stack);
            if (emc.equalsZero()) {
                unknownItems.add(stack.getItem());
                unknownMult += stack.getCount();
            }
            else
                sum.subtract(emc);
        }
        for (Ingredient ingredient : equation.input) {
            SuperNumber emc = getIngredientEmc(ingredient, equation.name);
            if (emc.equalsZero()) {
                for (ItemStack stack : ingredient.getMatchingStacks())
                    unknownItems.add(stack.getItem());
                unknownMult -= getIngredientCount(ingredient);
            }
            else
                sum.add(emc);
        }

        if (unknownMult == 0) {
            warn("Could not solve weird recipe: " + equation.name);
            return;
        }

        SuperNumber unknownWorth = new SuperNumber(sum);
        unknownWorth.divide(unknownMult);
        for (Item item : unknownItems) {
            if (item.hasRecipeRemainder()) {
                // if the item has a recipe remainder, the EMC value of the remainder 
                // should be added on top of the value this recipe assigns to the item.
                // (and if the remainder is equal to the item, we don't need to do this)
                Item remainder = item.getRecipeRemainder();
                if (!remainder.equals(item)) {
                    SuperNumber remainderEmc = getItemEmc(remainder);
                    if (remainderEmc.equalsZero()) 
                        continue;
                    remainderEmc.add(unknownWorth);
                    putEmcMap(item, remainderEmc, equation.name);
                    continue;
                }
            }
            putEmcMap(item, unknownWorth, equation.name);
        }
    }




    private boolean putEmcMap(Item item, SuperNumber value, String name) {
        if (!emcMapHasEntry(item)) {
            String itemName = itemName(item);
            unlock(itemName);
            emcMap.put(itemName, value);
            return true;
        }
        SuperNumber emc = getItemEmc(item);
        if (emc.equalTo(value))
            return false;
        warn("EMC Conflict for item " + itemName(item) 
            + ", EMC Mapper tried assigning two different values! Original value: " + emc + ", new value: " + value
            + ", current recipe: " + name);
        return false;
    }


    private SuperNumber getItemEmc(Item item) {
        if (item == null)
            return SuperNumber.Zero(); 
        String id = itemName(item);
        if (emcMap.containsKey(id)) 
            return new SuperNumber(emcMap.get(id));
        return SuperNumber.Zero(); 
    }
    private SuperNumber getItemStackEmc(ItemStack itemStack) {
        if (itemStack.isEmpty())
            return SuperNumber.Zero(); 
        Item item = itemStack.getItem();
        SuperNumber emc = getItemEmc(item);
        emc.multiply(itemStack.getCount());
        //considerStackDurability(itemStack, emc);
        //considerStackNbt(itemStack, emc);
        return emc;
    }



    private SuperNumber getIngredientEmc(Ingredient ingredient, String equationName) {
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            SuperNumber emc = getItemStackEmc(stack);
            if (emc.equalsZero())
                continue;

            for (ItemStack stack2 : ingredient.getMatchingStacks()) {
                putEmcMap(stack2.getItem(), emc, equationName);
            }
            return emc;
        }
        return SuperNumber.Zero();
    }
    private int getIngredientCount(Ingredient ingredient) {
        // UNCONSIDERED: Ingredients with matching itemstacks of different counts
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            return stack.getCount();
        }
        return 1;
    }

    private void warn(String input) {
        FabricatedExchange.LOGGER.warn(input);
        warninged = true;
    }

    private void unlock(String itemName) {
        if (!unknownEquationMap.containsKey(itemName))
            return;
        for (UnknownEquationNode node : unknownEquationMap.get(itemName)) {
            if (node.subtracted == true)
                continue;
            node.subtracted = true;
            ItemEquation equation = node.equation;
            equation.amountUnknown--;
            if (equation.amountUnknown == 1) {
                if (splitQueues.containsKey(equation.origin))
                    splitQueues.get(equation.origin).add(equation);
                else {
                    Queue<ItemEquation> modQueue = new LinkedList<ItemEquation>();
                    modQueue.add(equation);
                    splitQueues.put(equation.origin, modQueue);
                }
            }
        }
        unknownEquationMap.remove(itemName);
    }


    private boolean iteratePotionRecipe(BrewingRecipeAccessor<Potion> recipe) {
        boolean newInfo = false;

        Potion inputPotion = recipe.getInput();
        Ingredient ingredient = recipe.getIngredient();
        Potion outputPotion = recipe.getOutput();

        SuperNumber inputEmc = getPotionEmc(inputPotion);
        
        if (inputEmc.isNegative())
            return false;

        ItemStack[] stacks = ingredient.getMatchingStacks();
        SuperNumber ingredientEmc = SuperNumber.ZERO;
        for (ItemStack stack : stacks) {
            Item item = stack.getItem();
            ingredientEmc = getItemEmc(item);
            if (ingredientEmc.equalsZero())
                continue;
            for (ItemStack stack2 : stacks) {
                Item item2 = stack2.getItem();
                if (!item.equals(item2)) 
                    newInfo = putEmcMap(item2, ingredientEmc, null) || newInfo;
            }
        }
        if (ingredientEmc.equalsZero())
            return false;
        
        ingredientEmc.divide(3);
        inputEmc.add(ingredientEmc);
        inputEmc.floor();

        putPotionEmcMap(outputPotion, inputEmc);


        return newInfo;
    }


    private boolean putPotionEmcMap(Potion potion, SuperNumber value) {
        String id = Registry.POTION.getId(potion).toString();
        if (!potionEmcMap.containsKey(id)) {
            potionEmcMap.put(id, value);
            return true;
        }
        return false;
    }

    // Potions, unlike items, return -1 when the map doesn't contain the potion
    private SuperNumber getPotionEmc(Potion potion) {
        String id = Registry.POTION.getId(potion).toString();
        if (potionEmcMap.containsKey(id)) 
            return new SuperNumber(potionEmcMap.get(id));
        return SuperNumber.NegativeOne(); 
    }

    private class UnknownEquationNode {
        public ItemEquation equation;
        public boolean subtracted = false;
        public UnknownEquationNode(ItemEquation equation) {
            this.equation = equation;
        }
    }
}

package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.BrewingRecipeAccessor;
import com.skirlez.fabricatedexchange.mixin.BrewingRecipeRegistryAccessor;
import com.skirlez.fabricatedexchange.mixin.IngredientAccessor;
import com.skirlez.fabricatedexchange.mixin.LegacySmithingRecipeAccessor;
import com.skirlez.fabricatedexchange.mixin.TagEntryAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.EqualTagsFile;
import com.skirlez.fabricatedexchange.util.config.ModConfig;
import com.skirlez.fabricatedexchange.util.config.ModifiersFile;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;


// This class tries to infer EMC values of items through in-game recipes.

// In areas of code where a possible rare case is not considered, 
// a comment prefixed with "UNCONSIDERED:" should be left

public class EmcMapper {
    private ConcurrentMap<String, SuperNumber> emcMap;
    private ConcurrentMap<String, SuperNumber> potionEmcMap;

    private boolean warninged = false;
    
    private RecipeManager recipeManager;
    private Map<Item, Set<MapperAction>> unknownEquationMap = new HashMap<Item, Set<MapperAction>>();
    private PriorityQueue<MapperAction> queue = new PriorityQueue<MapperAction>();

    private Set<MapperAction> queuedActionsSet = new HashSet<MapperAction>();

    private final ModifiersFile modifiers;
    private final EqualTagsFile equalTags;
    public EmcMapper(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;

        emcMap = new ConcurrentHashMap<String, SuperNumber>();
        potionEmcMap = new ConcurrentHashMap<String, SuperNumber>();

        modifiers = ModConfig.MODIFIERS;
        equalTags = ModConfig.EQUAL_TAGS;
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
        List<StonecuttingRecipe> allStonecuttingRecipes = recipeManager.listAllOfType(RecipeType.STONECUTTING);

        // blacklisted recipes and items
        Map<String, HashSet<String>> blacklistedRecipes = ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.getValue();
        if (blacklistedRecipes == null)
            blacklistedRecipes = new HashMap<>();
        HashSet<String> smithingRecipesBlacklist = blacklistedRecipes.getOrDefault("smithing", new HashSet<String>());
        HashSet<String> smeltingRecipesBlacklist = blacklistedRecipes.getOrDefault("smelting", new HashSet<String>());
        HashSet<String> craftingRecipesBlacklist = blacklistedRecipes.getOrDefault("crafting", new HashSet<String>());
        HashSet<String> stonecuttingRecipesBlacklist = blacklistedRecipes.getOrDefault("stonecutting", new HashSet<String>());
        // we must split the item equations between their origin mod. 
        // minecraft item equations should evaluate first, and then we don't actually care about the order.

        convertRecipesToEquations(allSmithingRecipes, smithingRecipesBlacklist, this::createEquation);
        convertRecipesToEquations(allSmeltingRecipes, smeltingRecipesBlacklist, this::createEquation);
        convertRecipesToEquations(allCraftingRecipes, craftingRecipesBlacklist, this::createEquation);
        convertRecipesToEquations(allStonecuttingRecipes, stonecuttingRecipesBlacklist, this::createEquation);

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
                Arrays.asList(new ItemStack(output, 3)), 
                new Identifier(name));
                
            processEquation(equation);
        }

        // TODO: inject recipes provided by mods here

    

        List<List<Item>> itemGroups = equalTags.getItemGroups(); 
        for (List<Item> list : itemGroups) {
            MapperAction action = new EqualizeTagAction(list);
            for (Item item : list) {
                registerAction(item, action);
            }
        }


        while (!queue.isEmpty()) {
            MapperAction action = queue.poll();
            action.perform();
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
        return new ItemEquation(filteredIngredients, output, recipe.getId());
    }

    private ItemEquation createEquation(SmeltingRecipe recipe) {
        return new ItemEquation(
            Arrays.asList(recipe.getIngredients().get(0)),
            Arrays.asList(recipe.getOutput()),
            recipe.getId());
    }


    @Nullable
    private ItemEquation createEquation(SmithingRecipe recipe) {
        LegacySmithingRecipeAccessor recipeAccessor = (LegacySmithingRecipeAccessor) recipe;
        return new ItemEquation(
            Arrays.asList(recipeAccessor.getBase(), recipeAccessor.getAddition()),
            Arrays.asList(recipe.getOutput()),
            recipe.getId());
    }


    private ItemEquation createEquation(StonecuttingRecipe recipe) {
        return new ItemEquation(
            recipe.getIngredients(),
            Arrays.asList(recipe.getOutput()),
            recipe.getId());
    }



    private <T extends Recipe<?>> void convertRecipesToEquations(List<T> allRecipes, HashSet<String> blacklist, 
            Function<T, ItemEquation> equationConvertion) {
        for (int i = 0; i < allRecipes.size(); i++) {
            T recipe = allRecipes.get(i);
            if (blacklist.contains(recipe.getId().toString()))
                continue;
            ItemEquation equation = equationConvertion.apply(recipe);
            if (equation == null)
                continue;
            processEquation(equation);
        }
    }


    private boolean emcMapHasEntry(Item item) {
        return emcMap.containsKey(itemName(item));
    }

    private String itemName(Item item) {
        return Registry.ITEM.getId(item).toString();
    }

    // Counts the unknowns of the equation and also simplifies it down if it has ingredients with a tag marked as an equal tag.
    public void processEquation(ItemEquation equation) {
        Set<Item> unknownItems = new HashSet<Item>();
        for (int i = 0; i < equation.input.size(); i++) {
            Ingredient ingredient = equation.input.get(i);
            final Ingredient.Entry[] entries = ((IngredientAccessor)(Object)ingredient).getEntries();
            for (Ingredient.Entry entry : entries) {
                if (!(entry instanceof Ingredient.TagEntry))
                    continue;
                TagKey<Item> tag = ((TagEntryAccessor)(Object)entry).getTag();
                if (equalTags.hasTag(tag)) {
                    Item item = GeneralUtil.getAnyItemFromItemTag(tag);
                    ItemStack stack = new ItemStack(item, getIngredientCount(ingredient));
                    ingredient = Ingredient.ofStacks(stack);
                    equation.input.set(i, ingredient);
                }
            }

            SuperNumber value = getIngredientEmc(ingredient);
            if (value.equalsZero()) {
                for (ItemStack stack : ingredient.getMatchingStacks())
                    unknownItems.add(stack.getItem());
            }
        }
        for (ItemStack stack : equation.output) {
            if (!emcMapHasEntry(stack.getItem()))
                unknownItems.add(stack.getItem());
        }   
        int amountUnknown = unknownItems.size();
        MapperAction action = new SolveEquationAction(equation, amountUnknown);
        for (Item item : unknownItems)
            registerAction(item, action);
        if (amountUnknown <= 1) {
            queue.add(action);
            queuedActionsSet.add(action);
        }
        


        
     
        
    }

    private void registerAction(Item item, MapperAction action) {
        if (!unknownEquationMap.containsKey(item)) {
            Set<MapperAction> set = new HashSet<MapperAction>();
            unknownEquationMap.put(item, set);
        }
        unknownEquationMap.get(item).add(action);
    }

    private void equalizeList(List<Item> items) {
        SuperNumber emc = SuperNumber.ZERO;
        for (Item item : items) {
            if (emcMapHasEntry(item)) {
                emc = getItemEmc(item);
                break;
            }
        }
        if (emc.equalsZero()) {
            FabricatedExchange.LOGGER.warn("Attempted to equalize tag without any items that have EMC! Skipping...");
            return;
        }
        for (Item item : items) {
            if (!emcMapHasEntry(item))
                putEmcMap(item, emc, null);
        }
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
            SuperNumber emc = getIngredientEmc(ingredient);
            if (emc.equalsZero()) {
                for (ItemStack stack : ingredient.getMatchingStacks())
                    unknownItems.add(stack.getItem());
                unknownMult -= getIngredientCount(ingredient);
            }
            else
                sum.add(emc);
        }

        if (unknownMult == 0) {
            warn("Could not solve weird recipe: " + equationName(equation));
            return;
        }

        SuperNumber unknownWorth = new SuperNumber(sum);
        unknownWorth.divide(unknownMult);
        for (Item item : unknownItems) {
            putEmcMap(item, unknownWorth, equation);
        }
    }

    private void verify(ItemEquation equation) {
        SuperNumber inputEmc = SuperNumber.Zero();
        SuperNumber outputEmc = SuperNumber.Zero();

        for (ItemStack stack : equation.output)
            outputEmc.add(getItemStackEmc(stack));

        for (Ingredient ingredient : equation.input)
            inputEmc.add(getIngredientEmc(ingredient));
        
        if (inputEmc.compareTo(outputEmc) < 0) {
            warn("Recipe EMC conflict for recipe: " + equationName(equation)
                + " recipe gives more EMC than you put in! Input: " + inputEmc + " Output: " + outputEmc);
        }

    }

    private String equationName(ItemEquation equation) {
        return (equation == null) ? "none" : (equation.origin + ":" + equation.name);
    }

    private boolean putEmcMap(Item item, SuperNumber value, ItemEquation equation) {
        if (value.compareTo(SuperNumber.ZERO) <= 0) {
            warn("EMC Mapper tried assigning item " + itemName(item) 
                + " a value lower or equal to 0. Current recipe: " + equationName(equation));
                return false;
        }
        if (!emcMapHasEntry(item)) {
            unlock(item);
            emcMap.put(itemName(item), value);
            return true;
        }
        SuperNumber emc = getItemEmc(item);
        if (emc.equalTo(value))
            return false;
        warn("Item EMC conflict for item " + itemName(item) 
            + ", EMC Mapper tried assigning two different values! Original value: " + emc + ", new value: " + value
            + ", current recipe: " + equationName(equation));
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



    private SuperNumber getIngredientEmc(Ingredient ingredient) {
        ItemStack[] arr = ingredient.getMatchingStacks();
        if (arr.length == 0)
            return SuperNumber.Zero();

        SuperNumber emc = SuperNumber.ZERO;
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            emc = getItemStackEmc(stack);
            if (emc.equalsZero())
                return SuperNumber.Zero();
        }
        return emc;
        
    }
    private int getIngredientCount(Ingredient ingredient) {
        // UNCONSIDERED: Ingredients with matching itemstacks of different counts
        for (ItemStack stack : ingredient.getMatchingStacks())
            return stack.getCount();
        return 1;
    }

    private void warn(String input) {
        FabricatedExchange.LOGGER.warn(input);
        warninged = true;
    }

    private void unlock(Item item) {
        if (!unknownEquationMap.containsKey(item))
            return;
        Set<MapperAction> set = unknownEquationMap.get(item);
        for (MapperAction action : set) {
            boolean add = action.feed(item);
            if (add && !queuedActionsSet.contains(action)) {
                queue.add(action);
                queuedActionsSet.add(action);
            }
        }
        unknownEquationMap.remove(item);
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

    // TODO: Return higher int for equations that will find inputs and not outputs
    private int getEquationPriority(ItemEquation equation) {
        return equation.origin.equals("minecraft") ? 0 : 1;
    }

    /** This class represents an action the mapper is queued to do. 
    Some actions have a higher (lower value) priority than others. */
    abstract class MapperAction implements Comparable<MapperAction> {
        private int priority;

        public MapperAction(int priority) {
            this.priority = priority;
        }

        /** When an item relevant to this action is given EMC, this action will be notified.
        // returning true means add to the queue. */
        public abstract boolean feed(Item item);

        public abstract void perform();

        @Override
        public int compareTo(MapperAction other) {
            return Integer.compare(this.priority, other.priority);
        }
    }

    class SolveEquationAction extends MapperAction {
        private int amountUnknown;
        private ItemEquation equation;
        public SolveEquationAction(ItemEquation equation, int amountUnknown) {
            super(getEquationPriority(equation));
            this.equation = equation;
            this.amountUnknown = amountUnknown;
        }
        
        @Override
        public boolean feed(Item item) {
            amountUnknown--;
            return amountUnknown == 1 || amountUnknown == 0;
        }

        @Override
        public void perform() {
            if (amountUnknown <= 0) {
                verify(equation);
                return;
            }
            solve(equation);
        }
    }
    class EqualizeTagAction extends MapperAction {
        private List<Item> tagItems;
        boolean doAnyItemsHaveEmc = false;
        public EqualizeTagAction(List<Item> tagItems) {
            super(3);
            this.tagItems = tagItems;
        }

        @Override
        public boolean feed(Item item) {
            return true;
        }

        @Override
        public void perform() {
            equalizeList(tagItems);
        }
        @Override
        public int compareTo(MapperAction other) {
            return super.compareTo(other);
        }
    }
}






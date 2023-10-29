package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

// NOTE: Code is in an *incredibly* bad state due to trying out many ideas related to backtracking very quickly. Won't be fixed in this branch!

/* This class tries to infer EMC values of items through in-game recipes. It maps items to EMC values.
Here's a rough outline of how it works:
1. Obtain all recipes from Minecraft
2. Convert those recipes into the more abstract ItemEquation class,
and in the future, also take in ItemEquations provided by other mods
3. Read the Seed EMC values
4. Count the amount of unknown items in each equation (due to ingredients, which can have multiple matching items, this is complex)
5. Add any equation with a single unknown to be solved, and no unknowns to be verified.
6. Once an item is given an EMC value, subtract from any relevant equation's unknown count, 
and add it to the queue if needed.
7. If in verification an ItemEquation gives more EMC that it inputs, choose an item (depending on the age, see below)
and change its EMC to make the equation equal again, then retroactively also update any items 
who's value is dependent on that item. This will be referred to as backtracking.
Note that backtracking is recursive, and that any item updated in this process should also backtrack.

It also infers EMC values of potion NBT data in a different simpler way.
*/


// In areas of code where a possible rare case is not considered, 
// a comment prefixed with "UNCONSIDERED:" should be left
public class EmcMapper {
    private Map<String, SuperNumber> emcMap;
    private Map<String, SuperNumber> potionEmcMap;


    private boolean warninged = false;
    
    private RecipeManager recipeManager;

    // This is a map between item names and equation nodes. Whenever a new item is assigned an EMC value,
    // the mapper should traverse to all the nodes in that item's set and subtract from the held node equation's unknown count.
    private Map<String, Set<UnknownEquationNode>> unknownEquationMap = new HashMap<String, Set<UnknownEquationNode>>();
    
    // Queue of equations to be solved or verified
    private Queue<ItemEquation> queue = new LinkedList<ItemEquation>();

    // This is a map between item names and the set of all equations that specific item had 
    // any influence in the value of its conclusions.
    private Map<String, Set<ItemEquation>> itemInfluenceMap = new HashMap<String, Set<ItemEquation>>();

    // This is a map between item equations and all the items it has given new EMC values to.
    private Map<ItemEquation, List<Item>> conclusionMap = new HashMap<ItemEquation, List<Item>>();

    // This is a map where items that have backtracked store their values.
    // To avoid infinite recursion with backtracking, if the EMC of an item goes in one direction on the number line, 
    // and then switches (increasing -> decreasing/decreasing -> increasing), we will stop trying to assign a value to the item.
    private Map<String, List<SuperNumber>> backtrackingMap = new HashMap<String, List<SuperNumber>>();

    /* During equation verification, if the input EMC is greater than the output EMC, we must modify one of the emc values
    of the items involved in the equation in order to equalize it. the question is, how do we choose? There is no right answer, 
    any item can be chosen to equalize the equation. but in most cases (i found that) you'd want it to be the item that we most recently found the EMC of.
    The age map attempts to solve this. Every time an item is given EMC, we give it the age in ageCount,
    and increase it by one. When selecting which item to backtrack for, the mapper will choose the item with the highest age. */
    private Map<String, Integer> ageMap = new HashMap<String, Integer>();
    private int ageCount;

    private final ModifiersFile modifiers;

    public EmcMapper(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;

        emcMap = new ConcurrentHashMap<String, SuperNumber>();
        potionEmcMap = new ConcurrentHashMap<String, SuperNumber>();

        modifiers = ModConfig.MODIFIERS;
    }

    public Map<String, SuperNumber> getEmcMap() {
        return emcMap;
    }
    public Map<String, SuperNumber> getPotionEmcMap() {
        return potionEmcMap;
    }


    /** Maps out all the recipes known to recipeManager + item equations given by mods. */
    public boolean map() {

        Map<String, SuperNumber> seedEmcMap = ModConfig.SEED_EMC_MAP_FILE.getValue();
        if (seedEmcMap != null)
            GeneralUtil.mergeMap(emcMap, seedEmcMap);

        for (String itemName : seedEmcMap.keySet()) {
            ageMap.put(itemName, ageCount++);
        }

        List<SmithingRecipe> allSmithingRecipes = recipeManager.listAllOfType(RecipeType.SMITHING);
        List<SmeltingRecipe> allSmeltingRecipes = recipeManager.listAllOfType(RecipeType.SMELTING);
        List<CraftingRecipe> allCraftingRecipes = recipeManager.listAllOfType(RecipeType.CRAFTING);
        List<StonecuttingRecipe> allStonecuttingRecipes = recipeManager.listAllOfType(RecipeType.STONECUTTING);
        
        Map<String, HashSet<String>> blacklistedRecipes = ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.getValue();
        if (blacklistedRecipes == null)
            blacklistedRecipes = new HashMap<>();
        HashSet<String> smithingRecipesBlacklist = blacklistedRecipes.getOrDefault("smithing", new HashSet<String>());
        HashSet<String> smeltingRecipesBlacklist = blacklistedRecipes.getOrDefault("smelting", new HashSet<String>());
        HashSet<String> craftingRecipesBlacklist = blacklistedRecipes.getOrDefault("crafting", new HashSet<String>());
        HashSet<String> stonecuttingRecipesBlacklist = blacklistedRecipes.getOrDefault("stonecutting", new HashSet<String>());
      
        
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
                Collections.singletonList(new ItemStack(output, 3)), 
                new Identifier(name));
                
            countUnknowns(equation);
        }

        // TODO: inject recipes provided by mods here

    
        while (!queue.isEmpty()) {
            ItemEquation equation = queue.poll();
            if (equation.amountUnknown > 1)
                continue;   
            if (equation.amountUnknown == 0) {
                verify(equation);
                continue;
            }
            solve(equation);
        }
            

        // most potion recipes are even more special, so we need to take care of them separately.
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
            Collections.singletonList(recipe.getIngredients().get(0)),
            Collections.singletonList(recipe.getOutput()),
            recipe.getId());
    }


    @Nullable
    private ItemEquation createEquation(SmithingRecipe recipe) {
        if (!(recipe instanceof SmithingRecipe))
            return null;

        LegacySmithingRecipeAccessor recipeAccessor = (LegacySmithingRecipeAccessor) recipe;
        return new ItemEquation(
            Arrays.asList(recipeAccessor.getBase(), recipeAccessor.getAddition()),
            Collections.singletonList(recipe.getOutput()),
            recipe.getId());
    }


    private ItemEquation createEquation(StonecuttingRecipe recipe) {
        return new ItemEquation(
            recipe.getIngredients(),
            Collections.singletonList(recipe.getOutput()),
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
            if (!equation.input.isEmpty())
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
    public void countUnknowns(ItemEquation equation) {
        Set<String> unknownItems = new HashSet<String>();
        Set<Ingredient> unknownIngredients = new HashSet<Ingredient>();
        for (Ingredient ingredient : equation.input) {
            SuperNumber value = getIngredientEmc(ingredient, equation);
            if (value.equalsZero()) {
                boolean contains = false;
                for (Ingredient ingredient2 : unknownIngredients) {
                    if (Arrays.equals(ingredient.getMatchingStacks(), ingredient2.getMatchingStacks())) {
                        contains = true;
                        break;
                    }
                }
                if (!contains)
                    unknownIngredients.add(ingredient);
            }
        }
        for (ItemStack stack : equation.output) {
            if (!emcMapHasEntry(stack.getItem()))
                unknownItems.add(itemName(stack.getItem()));
        }   
        equation.amountUnknown = unknownItems.size() + unknownIngredients.size();
        
        if (equation.amountUnknown >= 0) {
            tryAddEquation(equation);
            if (equation.amountUnknown == 0)
                return;
            for (String item : unknownItems)
                registerUnknownEquation(item, equation);
            for (Ingredient ingredient : unknownIngredients)
                registerUnknownEquation(ingredient, equation);
        }
    }
    private void solve(ItemEquation equation) {
        SuperNumber sum = SuperNumber.Zero();
        Set<Item> itemsInvolved = new HashSet<Item>();
        List<Item> unknownItems = new ArrayList<Item>();
        int unknownMult = 0;
        for (ItemStack stack : equation.output) {
            SuperNumber emc = getItemStackEmc(stack);
            if (emc.equalsZero()) {
                unknownItems.add(stack.getItem());
                unknownMult += stack.getCount();
            }
            else {
                sum.subtract(emc);
                itemsInvolved.add(stack.getItem());
            }
        }
        for (Ingredient ingredient : equation.input) {
            SuperNumber emc = getIngredientEmc(ingredient, equation);
            if (emc.equalsZero()) {
                for (ItemStack stack : ingredient.getMatchingStacks())
                    unknownItems.add(stack.getItem());
                unknownMult -= getIngredientCount(ingredient);
            }
            else {
                sum.add(emc);
                for (ItemStack stack : ingredient.getMatchingStacks())
                    itemsInvolved.add(stack.getItem());
            }
        }

        if (unknownMult == 0) {
            warn("Could not solve weird recipe: " + equation.origin + ":" + equation.name);
            return;
        }

        SuperNumber unknownWorth = new SuperNumber(sum);
        unknownWorth.divide(unknownMult);

        for (Item item : itemsInvolved) {
            String name = itemName(item);
            Set<ItemEquation> set = itemInfluenceMap.get(name);
            if (set == null) {
                set = new HashSet<ItemEquation>();
                itemInfluenceMap.put(name, set);
            }
            set.add(equation);
        }
        conclusionMap.put(equation, unknownItems);
        for (Item item : unknownItems) {
            putEmcMap(item, unknownWorth, equation);
        }
    }

    private void verify(ItemEquation equation) {
        SuperNumber inputEmc = SuperNumber.Zero();
        SuperNumber outputEmc = SuperNumber.Zero();

        List<Item> itemsInvolved = new ArrayList<Item>();
        

        for (ItemStack stack : equation.output) {
            outputEmc.add(getItemStackEmc(stack));
            itemsInvolved.add(stack.getItem());
        }

        for (Ingredient ingredient : equation.input) {
            for (ItemStack stack : ingredient.getMatchingStacks())
                itemsInvolved.add(stack.getItem());
            inputEmc.add(getIngredientEmc(ingredient, equation));
        }
        
        for (Item item : itemsInvolved) {
            String name = itemName(item);
            Set<ItemEquation> set = itemInfluenceMap.get(name);
            if (set == null) {
                set = new HashSet<ItemEquation>();
                itemInfluenceMap.put(name, set);
            }
            set.add(equation);
        }
        if (inputEmc.compareTo(outputEmc) < 0) {
            //warn("started backtracking for " + equation.origin + ":" + equation.name);
            // find the item with the highest age and CRUSH ITS BALLS
            Item maxItemI = null;
            Item maxItemO = null;
            int maxAgeI = 0;
            int maxAgeO = 0;

            int multiplierI = 1;
            int multiplierO = 1;

            SuperNumber sum = new SuperNumber(inputEmc);
            sum.add(outputEmc);

            for (int i = 0; i < equation.output.size(); i++) {
                ItemStack stack = equation.output.get(i);
                Item item = stack.getItem();
                int age = getItemAge(item);
                if (age == maxAgeO) {
                    multiplierO += stack.getCount();
                }
                else if (age > maxAgeO) {
                    maxAgeO = age;
                    maxItemO = item;
                    multiplierO = stack.getCount();
                }
                
            }

            for (int i = 0; i < equation.input.size(); i++) {
                Ingredient ingredient = equation.input.get(i);
                int age = getIngredientAge(ingredient);
                if (age == maxAgeI) {
                    multiplierI += getIngredientCount(ingredient);
                }
                if (age > maxAgeI) {
                    maxAgeI = age;
                    maxItemI = getIngredientMinimumItem(ingredient);
                    multiplierI = getIngredientCount(ingredient);
                }
            }

            boolean isOutput = (maxAgeO >= maxAgeI);
            if (isOutput) {
                SuperNumber emc = getItemEmc(maxItemO);
                emc.multiply(multiplierO);

                sum.subtract(outputEmc);
                outputEmc.subtract(emc);
                sum.subtract(outputEmc);
                sum.divide(multiplierO);
                putEmcMap(maxItemO, sum, equation);
            }
            else {
                SuperNumber emc = getItemEmc(maxItemI);
                emc.multiply(multiplierI);
        
                sum.subtract(inputEmc);
                inputEmc.subtract(emc);
                sum.subtract(inputEmc);
                sum.divide(multiplierI);
                putEmcMap(maxItemI, sum, equation);
            }

            /*
            else {
                warn("Recipe EMC conflict for recipe " + equation.origin + ":" + equation.name + "."
                    + " This recipe gives more EMC than you put in! Input: " + inputEmc + ", Output: " + outputEmc + ".");
            }
            */
        }

    }



    private SuperNumber getLastBacktrackValue(Item item) {
        List<SuperNumber> list = backtrackingMap.get(itemName(item));
        return list.get(list.size() - 1);
    }
    private void addBacktrackValue(Item item, SuperNumber newValue) {
        List<SuperNumber> list = backtrackingMap.get(itemName(item));
        if (list == null) {
            list = new ArrayList<SuperNumber>();
            backtrackingMap.put(itemName(item), list);
        }
        if (list.size() != 0) {
            if (newValue.equalTo(list.get(list.size() -1)))
                return;
        }

        list.add(newValue);
    }

    private boolean checkBacktrackConflicts(Item item, SuperNumber newValue) {
        List<SuperNumber> list = backtrackingMap.get(itemName(item));
        int size = list.size();
        if (size == 1)
            return false;
        SuperNumber lastInList = list.get(size - 1);
        SuperNumber delta = new SuperNumber(lastInList);
        delta.subtract(list.get(size - 2));
        
        SuperNumber newDelta = new SuperNumber(newValue);
        newDelta.subtract(lastInList);


        return newDelta.compareTo(SuperNumber.ZERO) != delta.compareTo(SuperNumber.ZERO);
    }



    private boolean putEmcMap(Item item, SuperNumber value, ItemEquation equation) {

        if (value.compareTo(SuperNumber.ZERO) <= 0) {
            warn("EMC Mapper tried assigning item " + itemName(item) 
                + " a value lower than or equal to 0. Current recipe: " + equation.origin + ":" + equation.name);
            
            return false;
        }
        SuperNumber emc;
        if (!backtrackingMap.containsKey(itemName(item))) {
            if (!emcMapHasEntry(item)) {
                //warn("placing INITIAL value " + value.divisionString() + " for " + itemName(item) + " equation " + equation.origin + ":" + equation.name);
                String itemName = itemName(item);
                unlock(itemName);
                emcMap.put(itemName, new SuperNumber(value));
                ageMap.put(itemName, ageCount++);
                return true;
            }
            emc = getItemEmc(item);
            if (value.compareTo(emc) == 0)
                return false;
        }
        else {
            SuperNumber lastValue = getLastBacktrackValue(item);
            if (lastValue == SuperNumber.NEGATIVE_ONE)
                return false;
            if (value.equalTo(lastValue)) {
                //warn("redundant backtrack for " + itemName(item) + " remains at " + value.divisionString());
                emcMap.put(itemName(item), value);
                
                return true;
            }
            if (checkBacktrackConflicts(item, value)) {
                String name = itemName(item);
                List<SuperNumber> list = backtrackingMap.get(name);
                String previousValues = "";
                for (SuperNumber previousValue : list) {
                    previousValues += previousValue.divisionString() + " ";
                }
                warn("WARNING: Item " + name + " deemed unusable. Backtracking list is not ordered: " + previousValues);
                relock(name);
                emcMap.remove(name);
                addBacktrackValue(item, SuperNumber.NEGATIVE_ONE);
                return true;
            }
        }
        //warn("placing value " + value.divisionString() + " for " + itemName(item) + " equation " + equation.origin + ":" + equation.name);
        String itemName = itemName(item);
        addBacktrackValue(item, getItemEmc(item));
        emcMap.put(itemName, new SuperNumber(value));
        ageMap.put(itemName(item), ageCount++);
        addBacktrackValue(item, getItemEmc(item));
        if (!itemInfluenceMap.containsKey(itemName)) {
            //warn("backtracking end for " + itemName(item));
            return true;
        }
        Set<ItemEquation> relatedEquations = new HashSet<ItemEquation>(itemInfluenceMap.get(itemName));
        for (ItemEquation relatedEquation : relatedEquations) {
            if (equation == relatedEquation)
                continue;

            List<Item> concludedItems = conclusionMap.get(relatedEquation);
            if (concludedItems == null)
                verify(relatedEquation);
            else {
                boolean solveable = true;
                for (Item concludedItem : concludedItems) {
                    String name = itemName(concludedItem);
                    if (backtrackingMap.containsKey(name)) {
                        SuperNumber lastValue = getLastBacktrackValue(concludedItem);
                        if (lastValue == SuperNumber.NEGATIVE_ONE) {
                            solveable = false;
                            break;
                        }
                    }
                    addBacktrackValue(concludedItem, getItemEmc(concludedItem));
                    emcMap.remove(name);
                    //warn("cleaned " + name + " while backtracking for " + itemName(item) + " in equation " + relatedEquation.origin + ":" + relatedEquation.name);
                }
                if (solveable)
                    solve(relatedEquation);
            }
        }
        //warn("backtracking end for " + itemName(item));
        return true;
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


    private SuperNumber getIngredientEmc(Ingredient ingredient, ItemEquation equation) {
        SuperNumber minEmc = null;
        List<Item> zeroItems = new ArrayList<Item>();
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            SuperNumber emc = getItemStackEmc(stack);
            if (emc.equalsZero()) {
                zeroItems.add(stack.getItem());
                continue;
            }
            if (minEmc == null)
                minEmc = emc;
            else 
                minEmc = SuperNumber.min(minEmc, emc);
        }

        if (minEmc == null)
            return SuperNumber.Zero();
        for (Item item : zeroItems)
            putEmcMap(item, minEmc, equation);  
        return minEmc;
    }
    private int getIngredientCount(Ingredient ingredient) {
        // UNCONSIDERED: Ingredients with matching itemstacks of different counts
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            return stack.getCount();
        }
        return 1;
    }
    private int getIngredientAge(Ingredient ingredient) {
        Item item = getIngredientMinimumItem(ingredient);
        return getItemAge(item);
    }
    private int getItemAge(Item item) {
        return ageMap.getOrDefault(itemName(item), Integer.MAX_VALUE);
    }


    private Item getIngredientMinimumItem(Ingredient ingredient) {
        SuperNumber minEmc = null;
        Item item = null;
        for (int i = 0; i < ingredient.getMatchingStacks().length; i++) {
            ItemStack stack = ingredient.getMatchingStacks()[i];
            SuperNumber emc = getItemStackEmc(stack);
            if (emc.equalsZero()) {
                continue;
            }
            else if (minEmc == null || emc.compareTo(minEmc) == -1) {
                minEmc = emc;
                item = stack.getItem();
            }
        }
        return item;
    }



    private void warn(String input) {
        FabricatedExchange.LOGGER.warn(input);
        warninged = true;
    }

    private void unlock(String itemName) {
        if (!unknownEquationMap.containsKey(itemName))
            return;
        Set<UnknownEquationNode> set = unknownEquationMap.get(itemName);
        for (UnknownEquationNode node : set) {
            if (node.subtracted == true)
                continue;
            node.subtracted = true;
            ItemEquation equation = node.equation;
            equation.amountUnknown--;
            tryAddEquation(equation);
        }
    }
    private void relock(String itemName) {
        if (!unknownEquationMap.containsKey(itemName))
            return;
        Set<UnknownEquationNode> set = unknownEquationMap.get(itemName);
        for (UnknownEquationNode node : set) {
            if (node.subtracted == false)
                continue;

            ItemEquation equation = node.equation;
            equation.amountUnknown = 999;
        }
    }



    private void tryAddEquation(ItemEquation equation) {
        if (equation.amountUnknown <= 1) {
            queue.add(equation);
        }
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

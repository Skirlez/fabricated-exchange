package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.LegacySmithingRecipeAccessor;
import com.skirlez.fabricatedexchange.util.CollectionUtil;
import com.skirlez.fabricatedexchange.util.ModConfig;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.LegacySmithingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public class EmcMapper {
    private ConcurrentMap<String, SuperNumber> emcMap;
    private String log;
    private String[] ignoredItems;
    public EmcMapper() {
        emcMap = new ConcurrentHashMap<String, SuperNumber>();
        log = "";
        ignoredItems = new String[0];
    }

    public ConcurrentMap<String, SuperNumber> getMap() {
        return emcMap;
    }
    public String getLog() {
        return log;
    }


    public void fillEmcMap(World world, RecipeManager recipeManager) {
        /*
        This function will fill the emc map - the thing that sets the relation between items and their emc values.
        this implementation is dumb, because I am not experienced enough to implement a better solution.
        it loops over all the recipes in the game a LOT to generate the mappings.
        */        
        Map<String, SuperNumber> seedEmcMap = ModConfig.SEED_EMC_MAP_FILE.getValue();
        if (seedEmcMap != null)
            CollectionUtil.mergeMap(emcMap, seedEmcMap);
        

        
        // i don't know what this thing is, but you need it for some functions
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();

        LinkedList<SmithingRecipe> smithingRecipes = new LinkedList<SmithingRecipe>(recipeManager.listAllOfType(RecipeType.SMITHING));

        LinkedList<SmeltingRecipe> smeltingRecipes = new LinkedList<SmeltingRecipe>(recipeManager.listAllOfType(RecipeType.SMELTING));
            
        LinkedList<CraftingRecipe> craftingRecipes = new LinkedList<CraftingRecipe>(recipeManager.listAllOfType(RecipeType.CRAFTING));


        // blacklisted recipes and items
        Map<String, String[]> blacklistedRecipes = ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.getValue();
        if (blacklistedRecipes != null) {
            String[] smeltingRecipesBlacklist = blacklistedRecipes.getOrDefault("smelting", new String[] {});
            removeArrayFromRecipeList(smeltingRecipesBlacklist, smeltingRecipes);

            String[] craftingRecipesBlacklist = blacklistedRecipes.getOrDefault("crafting", new String[] {});
            removeArrayFromRecipeList(craftingRecipesBlacklist, craftingRecipes);

            ignoredItems = blacklistedRecipes.getOrDefault("items", new String[] {});
        }

        for (int i = 0; i < 4; i++) {
            // Smithing recipes
            for (int j = 0; j < 100; j++) { 
                if (!iterateSmithingRecipes(recipeManager, dynamicRegistryManager, smithingRecipes))
                    break;
            }
            // Smelting recipes
            for (int j = 0; j < 100; j++) { 
                if (!iterateSmeltingRecipes(recipeManager, dynamicRegistryManager, smeltingRecipes))
                    break;
            }
            // Crafting recipes
            for (int j = 0; j < 100; j++) {
                if (!iterateCraftingRecipes(recipeManager, dynamicRegistryManager, craftingRecipes))
                    break;
            }
        }
    }


    private boolean iterateCraftingRecipes(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager, LinkedList<CraftingRecipe> recipesList) { 
        boolean newInfo = false;
        
        Iterator<CraftingRecipe> iterator = recipesList.iterator();
        while (iterator.hasNext()) {
            CraftingRecipe recipe = iterator.next();

            int unknownSide = 0; // 0 - unset, 1 - left, 2 - right
            List<Item> unknownItems = new ArrayList<Item>();
            boolean giveUp = false;
            List<Ingredient> ingredients = recipe.getIngredients();
            ItemStack outputStack = recipe.getOutput(dynamicRegistryManager);
            Item outputItem = outputStack.getItem();
            if (outputStack.getCount() == 0)
                continue; 

            SuperNumber outputEmc = getItemEmc(outputItem);
            outputEmc.multiply(outputStack.getCount());
            Pair<SuperNumber, SuperNumber> equation = new Pair<SuperNumber, SuperNumber>(SuperNumber.Zero(), SuperNumber.Zero());
            if (outputEmc.equalsZero())
                unknownSide = 2;
            else
                equation.getRight().add(outputEmc);
            for (Ingredient ingredient : ingredients) {
                ItemStack[] itemStackArr = ingredient.getMatchingStacks();
                if (itemStackArr.length == 0)
                    continue;
                SuperNumber emcWorth = SuperNumber.Zero();

                for (ItemStack stack : itemStackArr) {
                    Item item = stack.getItem();
                    SuperNumber itemEmc = getItemEmc(item);
                    if (itemEmc.equalsZero())
                        continue;
                    if (item.hasRecipeRemainder()) {
                        Item remainder = item.getRecipeRemainder();
                        itemEmc.subtract(getItemEmc(remainder));
                        if (itemEmc.compareTo(SuperNumber.ZERO) == -1) {
                            error("Negative EMC value upon subtracting recipe remainder! Recipe: " + recipe.getId().toString());
                            giveUp = true;
                            break;
                        }
                        if (itemEmc.equalsZero())
                            continue;
                    }
                    emcWorth = itemEmc;
                    
                }
                if (emcWorth.equalsZero()) {
                    if (unknownSide != 0) { // if there's already another unknown
                        giveUp = true;
                        break;
                    }
                    for (ItemStack stack : itemStackArr) {
                        unknownItems.add(stack.getItem());
                    }
                    unknownSide = 1;
                }
                else {
                    for (ItemStack stack : itemStackArr) {
                        Item item = stack.getItem();
                        SuperNumber itemEmc = getItemEmc(item);
                        if (itemEmc.equalsZero()) {
                            newInfo = putEmcMap(item, emcWorth, recipe);
                            break;
                        }
                    }
                    equation.getLeft().add(emcWorth);
                }
                
            }
            if (giveUp)
                continue;

            if (unknownSide == 0) { // if we know the emc value of everything, move on
                // TODO run inequality check
                continue;
            }
            else if (unknownSide == 1) {
                SuperNumber leftValue = equation.getLeft();
                SuperNumber rightValue = equation.getRight();
                rightValue.subtract(leftValue);
                if (rightValue.compareTo(SuperNumber.ZERO) == -1) {
                    error("Negative EMC value! Recipe: " + recipe.getId().toString());
                } 
                for (Item item : unknownItems) {
                    if (rightValue.equalsZero()) {
                        if (!isIgnoredItem(item))
                            warn("EMC Mapper thinks " + item.getTranslationKey() + " should have 0 EMC. Recipe: " + recipe.getId().toString());
                        continue;
                    }
                    newInfo = putEmcMap(item, rightValue, recipe);
                }
            }
            else {
                SuperNumber result = equation.getLeft();
                result.divide(outputStack.getCount());
                newInfo = putEmcMap(outputItem, result, recipe);
            }
        }
        return newInfo;
    }

    private boolean iterateSmeltingRecipes(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager, LinkedList<SmeltingRecipe> recipesList) {
        boolean newInfo = false;
        Iterator<SmeltingRecipe> iterator = recipesList.iterator();
        while (iterator.hasNext()) {
            SmeltingRecipe recipe = iterator.next();

            Ingredient inputIngredient = recipe.getIngredients().get(0);
            ItemStack[] itemStacks = inputIngredient.getMatchingStacks();
            SuperNumber inEmc = SuperNumber.Zero();
            int foundIndex = 0;
            for (int j = 0; j < itemStacks.length; j++) {
                ItemStack itemStack = itemStacks[j];
                SuperNumber itemEmc = getItemEmc(itemStack.getItem());
                if (itemEmc.equalsZero())
                    continue;
                if (inEmc.equalsZero()) {
                    inEmc = itemEmc;
                    foundIndex = j;
                    continue;
                }
                int comparison = itemEmc.compareTo(inEmc);
                if (comparison != 0) {
                    warn("EMC conflict in recipe " + recipe.getId().toString() + ": for items "  
                    + Registries.ITEM.getId(itemStacks[foundIndex].getItem()) + " with value " + inEmc + ", "
                    + Registries.ITEM.getId(itemStacks[j].getItem()) + " with value " + itemEmc
                    + ". choosing the lower value."); 
                    inEmc = SuperNumber.min(inEmc, itemEmc);
                }
            }
            Item outputItem = recipe.getOutput(dynamicRegistryManager).getItem();

            SuperNumber outEmc = getItemEmc(outputItem);
           
            if (outEmc.equalsZero()) {
                if (inEmc.equalsZero())
                    continue; // not enough info

                // if in emc is defined but out emc is not
                newInfo = putEmcMap(outputItem, inEmc, recipe);
            }
            else if (inEmc.equalsZero()) {
                 // if out emc is defined but in emc is not
                for (int j = 0; j < itemStacks.length; j++) {
                    Item inputItem = itemStacks[j].getItem();
                    newInfo = putEmcMap(inputItem, outEmc, recipe);
                }
            }
            else if (!inEmc.equalTo(outEmc)) { // we already know both inemc and outemc, run an inequality check
                warn("Inequality in recipe " + recipe.getId().toString());
            }
        }
        return newInfo;
    }


    private boolean iterateSmithingRecipes(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager, LinkedList<SmithingRecipe> recipesList) {
        boolean newInfo = false;

        Iterator<SmithingRecipe> iterator = recipesList.iterator();
        while (iterator.hasNext()) {
            // this is deprecated but at the moment the recipe list function gives us LegacySmithingRecipe,
            // when they change it we will properly use SmithingTransformRecipe and SmithingTrimRecipe
            LegacySmithingRecipe recipe = (LegacySmithingRecipe)iterator.next();

            LegacySmithingRecipeAccessor recipeAccessor = (LegacySmithingRecipeAccessor) recipe;
            Ingredient base = recipeAccessor.getBase();
            Ingredient addition = recipeAccessor.getAddition();
            Item a = base.getMatchingStacks()[0].getItem();
            Item b = addition.getMatchingStacks()[0].getItem();
            SuperNumber aEmc = getItemEmc(a);
            SuperNumber bEmc = getItemEmc(b);

            // if both aEmc and bEmc are zero, there is no point in progressing, since you wouldn't
            // know how to divide outEmc between the two (if it too wasn't zero)
            if (aEmc.equalsZero() && bEmc.equalsZero())
                continue; 


            Item output = recipe.getOutput(dynamicRegistryManager).getItem();
            SuperNumber outEmc = getItemEmc(output);


            if (outEmc.equalsZero()) {
                aEmc.add(bEmc);
                newInfo = putEmcMap(output, aEmc, recipe);
                continue;
            }
            if (aEmc.equalsZero()) {
                bEmc.add(outEmc);
                newInfo = putEmcMap(a, bEmc, recipe);
                continue;
            }
            outEmc.subtract(aEmc);
            newInfo = putEmcMap(b, outEmc, recipe);      
        }
        return newInfo;
    }

    private boolean isIgnoredItem(Item item) {
        return Arrays.stream(ignoredItems).anyMatch(s -> s.equals(Registries.ITEM.getId(item).toString()));
    }

    private boolean putEmcMap(Item item, SuperNumber value, Recipe<?> recipe) {
        if (value.equalsZero()) {
            warn("Conflict: EMC Mapper tried assigning item " + Registries.ITEM.getId(item).toString() 
            + " a value of 0. Current recipe: " + recipe.getId().toString());
            return false;
        }
        if (!emcMapHasEntry(item)) {
            emcMap.put(Registries.ITEM.getId(item).toString(), value);
            return true;
        }
        SuperNumber emc = getItemEmc(item);
        if (emc.equalTo(value))
            return false;
        warn("EMC Mapper tried assigning item " + Registries.ITEM.getId(item).toString() 
        + " two different value! Original value: " + emc + ", new value: " + value
        + ", current recipe: " + recipe.getId().toString());
        return false;
    }

    private SuperNumber getItemEmc(Item item) {
        if (item == null)
            return SuperNumber.Zero(); 
        String id = Registries.ITEM.getId(item).toString();
        if (emcMap.containsKey(id)) 
            return new SuperNumber(emcMap.get(id));
        return SuperNumber.Zero(); 
    }

    private boolean emcMapHasEntry(Item item) {
        return emcMap.containsKey(Registries.ITEM.getId(item).toString());
    }

    private void removeArrayFromRecipeList(String[] arr, LinkedList<?> list) {
        for (String str : arr) {
            int len = list.size();
            for (int i = 0; i < len; i++) {
                if (((Recipe<?>)list.get(i)).getId().toString().equals(str)) {
                    list.remove(i);
                    i--;
                    len--;
                }
            }
        }
    }

    private void warn(String input) {
        FabricatedExchange.LOGGER.warn(input);
        log += "WARNING: " + input + "\n";
    }
    private void error(String input) {
        FabricatedExchange.LOGGER.warn(input);
        log += "ERROR: " + input + "\n";
    }


}


package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.LegacySmithingRecipeAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
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
    private HashSet<String> log;

    public EmcMapper() {
        emcMap = new ConcurrentHashMap<String, SuperNumber>();
        log = new HashSet<String>();
    }

    public ConcurrentMap<String, SuperNumber> getMap() {
        return emcMap;
    }
    public String getLog() {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = log.iterator();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }


    public void fillEmcMap(World world, RecipeManager recipeManager) {
        /*
        This function will fill the emc map - the thing that sets the relation between items and their emc values.
        this implementation is dumb, because I am not experienced enough to implement a better solution.
        it loops over all the recipes in the game a LOT to generate the mappings.
        */        
        FabricatedExchange.LOGGER.info("Start EMC mapper");
        Map<String, SuperNumber> seedEmcMap = ModConfig.SEED_EMC_MAP_FILE.getValue();
        if (seedEmcMap != null)
            GeneralUtil.mergeMap(emcMap, seedEmcMap);
        
        // i don't know what this thing is, but you need it for some functions
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();

        LinkedList<SmithingRecipe> smithingRecipes = new LinkedList<SmithingRecipe>(recipeManager.listAllOfType(RecipeType.SMITHING));

        LinkedList<SmeltingRecipe> smeltingRecipes = new LinkedList<SmeltingRecipe>(recipeManager.listAllOfType(RecipeType.SMELTING));
        
        LinkedList<CraftingRecipe> craftingRecipes = new LinkedList<CraftingRecipe>(recipeManager.listAllOfType(RecipeType.CRAFTING));

        // blacklisted recipes and items
        Map<String, String[]> blacklistedRecipes = ModConfig.BLACKLISTED_MAPPER_RECIPES_FILE.getValue();
        if (blacklistedRecipes != null) {

            String[] smithingRecipesBlacklist = blacklistedRecipes.getOrDefault("smithing", new String[] {});
            removeArrayFromRecipeList(smithingRecipesBlacklist, smeltingRecipes);

            String[] smeltingRecipesBlacklist = blacklistedRecipes.getOrDefault("smelting", new String[] {});
            removeArrayFromRecipeList(smeltingRecipesBlacklist, smeltingRecipes);

            String[] craftingRecipesBlacklist = blacklistedRecipes.getOrDefault("crafting", new String[] {});
            removeArrayFromRecipeList(craftingRecipesBlacklist, craftingRecipes);
        }

        for (int i = 0; i < 100; i++) {
            // Smithing recipes
            int count = 0;
            for (int j = 0; j < 100; j++) { 
               if (!iterateSmithingRecipes(recipeManager, dynamicRegistryManager, smithingRecipes)) {
                    if (j == 0)
                        count++;
                    break;
                }
            }
            // Smelting recipes
            for (int j = 0; j < 100; j++) { 
                if (!iterateSmeltingRecipes(recipeManager, dynamicRegistryManager, smeltingRecipes)) {
                    if (j == 0)
                        count++;
                    break;
                }
            }
            // Crafting recipes
            for (int j = 0; j < 100; j++) {
                if (!iterateCraftingRecipes(recipeManager, dynamicRegistryManager, craftingRecipes)) {
                    if (j == 0)
                        count++;
                    break;
                }
            }
            if (count == 3) // if no information was extracted from an iteration
                break;
        }
        FabricatedExchange.LOGGER.info("End EMC mapper");
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
                if (itemStackArr.length == 0) // this will be zero for air
                    continue;
                SuperNumber emcWorth = SuperNumber.Zero();
                boolean unconsumedItem = false; // this will be true if an item's recipe remainder is equal in emc to itself
                for (ItemStack stack : itemStackArr) {
                    Item item = stack.getItem();
                    SuperNumber itemEmc = getItemEmc(item);
                    if (itemEmc.equalsZero())
                        continue;
                    if (item.hasRecipeRemainder()) {
                        Item remainder = item.getRecipeRemainder();
                        itemEmc.subtract(getItemEmc(remainder));
                        int comparison = itemEmc.compareTo(SuperNumber.ZERO);
                        if (comparison == -1) {
                            warn("Negative EMC value upon subtracting recipe remainder! Recipe: " + recipe.getId().toString());
                            giveUp = true;
                            break;
                        }
                        if (comparison == 0) {
                            unconsumedItem = true;
                            break;
                        }
                    }
                    emcWorth = itemEmc;
                }
                if (unconsumedItem)
                    continue;
                if (emcWorth.equalsZero()) {
                    if (unknownSide == 2) { // if the output is unknown as well
                        giveUp = true;
                        break;
                    }
                    else if (unknownSide == 0) { // if this is the first unknown we find
                        for (ItemStack stack : itemStackArr) {
                            unknownItems.add(stack.getItem());
                        }
                        unknownSide = 1;
                    }
                    else { // if this is the second unknown we find, make sure it isn't the same unknown as the first
                        for (ItemStack stack : itemStackArr) {
                            if (!unknownItems.contains(stack.getItem())) {
                                giveUp = true;
                                break;
                            }
                        }
                    }
                }
                else {
                    for (ItemStack stack : itemStackArr) {
                        Item item = stack.getItem();
                        SuperNumber itemEmc = getItemEmc(item);
                        if (itemEmc.equalsZero()) {
                            newInfo = putEmcMap(item, emcWorth, recipe) || newInfo;
                            break;
                        }
                    }
                    equation.getLeft().add(emcWorth);
                }
            }
            if (giveUp)
                continue;

            if (unknownSide == 0) { // if we know the emc value of everything
                if (!equation.getLeft().equalTo(equation.getRight()))
                    warn("Inequality detected in recipe " + recipe.getId().toString());
                continue;
            }
            else if (unknownSide == 1) {
                SuperNumber leftValue = equation.getLeft();
                SuperNumber rightValue = equation.getRight();
                rightValue.subtract(leftValue);
                if (rightValue.compareTo(SuperNumber.ZERO) == -1) {
                    warn("Negative EMC value! Recipe: " + recipe.getId().toString());
                    continue;
                } 
                for (Item item : unknownItems) {
                    if ((item.hasRecipeRemainder() && item.getRecipeRemainder().equals(item)))
                        break;
                    if (rightValue.equalsZero()) {
                        warn("EMC Mapper thinks " + item.getTranslationKey() + " should have 0 EMC. Recipe: " + recipe.getId().toString());
                        continue;
                    }
                    newInfo = putEmcMap(item, rightValue, recipe) || newInfo;
                }
            }
            else {
                SuperNumber result = equation.getLeft();
                result.divide(outputStack.getCount());
                newInfo = putEmcMap(outputItem, result, recipe) || newInfo;
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
                    warn("Ingredient found with multiple conflicting EMC values! in recipe " + recipe.getId().toString() + ": for items "  
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
                newInfo = putEmcMap(outputItem, inEmc, recipe) || newInfo;
            }
            else if (inEmc.equalsZero()) {
                 // if out emc is defined but in emc is not
                for (int j = 0; j < itemStacks.length; j++) {
                    Item inputItem = itemStacks[j].getItem();
                    newInfo = putEmcMap(inputItem, outEmc, recipe) || newInfo;
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
            Item output = recipe.getOutput(dynamicRegistryManager).getItem();
            SuperNumber outEmc = getItemEmc(output);

            int unknownCount = 0;
            if (aEmc.equalsZero())
                unknownCount++;
            if (bEmc.equalsZero())
                unknownCount++;
            if (outEmc.equalsZero())
                unknownCount++;

            if (unknownCount == 0) {
                // TODO: Conflict check
                continue;
            }
            if (unknownCount > 1)
                continue;

            if (outEmc.equalsZero()) {
                aEmc.add(bEmc);
                newInfo = putEmcMap(output, aEmc, recipe) || newInfo;
                continue;
            }
            if (aEmc.equalsZero()) {
                bEmc.add(outEmc);
                newInfo = putEmcMap(a, bEmc, recipe) || newInfo;
                continue;
            }
            outEmc.subtract(aEmc);
            newInfo = putEmcMap(b, outEmc, recipe) || newInfo;      
        }
        return newInfo;
    }

    private boolean putEmcMap(Item item, SuperNumber value, Recipe<?> recipe) {
        if (value.compareTo(SuperNumber.ZERO) <= 0) {
            warn("EMC Mapper tried assigning item " + Registries.ITEM.getId(item).toString() 
            + " a value lower or equal to 0. Current recipe: " + recipe.getId().toString());
            return false;
        }
        if (!emcMapHasEntry(item)) {
            emcMap.put(Registries.ITEM.getId(item).toString(), value);
            return true;
        }
        SuperNumber emc = getItemEmc(item);
        if (emc.equalTo(value))
            return false;
        warn("EMC Conflict for item " + Registries.ITEM.getId(item).toString() 
        + ". EMC Mapper tried assigning two different values! Original value: " + emc + ", new value: " + value
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
        if (!log.contains(input)) {
            FabricatedExchange.LOGGER.warn(input);
            log.add(input);
        }
    }

}


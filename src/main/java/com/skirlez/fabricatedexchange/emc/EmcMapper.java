package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.mixin.LegacySmithingRecipeAccessor;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    private Map<String, SuperNumber> emcMap;
    public EmcMapper(Map<String, SuperNumber> emcMap) {
        this.emcMap = emcMap;
    }

    public void fillEmcMap(World world, RecipeManager recipeManager) {
        /*
        This function will fill the emc map - the thing that sets the relation between items and their emc values.
        this implementation is dumb, because I am not experienced enough to implement a better solution.
        it loops over all the recipes in the game a LOT to generate the mappings.
        TODO: Save the result to make this less terrible.
        */
        emcMap.clear();


        // the seed values
        // TODO: read from json
        putEmcMap(Items.COBBLESTONE, 1);
        putEmcMap(Items.DIRT, 1);
        putEmcMap(Items.GRASS_BLOCK, 1);
        putEmcMap(Items.OAK_PLANKS,8);
        putEmcMap(Items.WHITE_WOOL, 48);
        putEmcMap(Items.BONE, 96);
        putEmcMap(Items.REDSTONE, 64);
        putEmcMap(Items.GLOWSTONE_DUST, 384);
        putEmcMap(Items.EMERALD, 16384);
        putEmcMap(Items.ENDER_PEARL, 1024);
        putEmcMap(Items.COAL, 32);
        putEmcMap(Items.IRON_INGOT, 256);
        putEmcMap(Items.GOLD_INGOT, 2048);
        putEmcMap(Items.NETHERITE_SCRAP, 12288);
        putEmcMap(Items.DIAMOND, 8192);
        putEmcMap(Items.NETHER_STAR, 139264);

        // blacklisted recipes
        String[] smeltingRecipesBlacklist = {"minecraft:iron_nugget_from_smelting", "minecraft:gold_nugget_from_smelting"};
        
        // i don't know what this thing is, but you need it for some functions
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();

        LinkedList<SmithingRecipe> smithingRecipes = new LinkedList<SmithingRecipe>(recipeManager.listAllOfType(RecipeType.SMITHING));

        LinkedList<SmeltingRecipe> smeltingRecipes = new LinkedList<SmeltingRecipe>(recipeManager.listAllOfType(RecipeType.SMELTING));
        removeArrayFromRecipeList(smeltingRecipesBlacklist, smeltingRecipes);
        LinkedList<CraftingRecipe> craftingRecipes = new LinkedList<CraftingRecipe>(recipeManager.listAllOfType(RecipeType.CRAFTING));


        for (int i = 0; i < 4; i++) {
            // Smelting recipes
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
        
        int len = recipesList.size();
        for (int i = 0; i < len; i++) {
            CraftingRecipe recipe = recipesList.get(i);
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
          


            if (outputEmc.equalsZero()) {
                unknownSide = 2;
            }
            else {
                equation.getRight().add(outputEmc);
            }
            

            for (Ingredient ingredient : ingredients) {
                ItemStack[] itemStackArr = ingredient.getMatchingStacks();
                if (itemStackArr.length == 0)
                    continue;
                SuperNumber emcWorth = SuperNumber.Zero();

                for (ItemStack stack : itemStackArr) {
                    Item item = stack.getItem();
                    SuperNumber itemEmc = getItemEmc(item);
                    if (!itemEmc.equalsZero()) {
                        emcWorth = itemEmc;
                        break;
                    }
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
                        if (itemEmc.equalsZero() && !emcMapHasEntry(item)) {
                            putEmcMap(item, emcWorth);
                            break;
                        }
                    }
                    equation.getLeft().add(emcWorth);
                }
                
            }
            if (giveUp)
                continue;

            

            if (unknownSide == 0) { // if we know the emc value of everything,  move on
                recipesList.remove(i);
                i--;
                len--;
                continue;
            }
            else if (unknownSide == 1) {
                SuperNumber leftValue = equation.getLeft();
                SuperNumber rightValue = equation.getRight();
                rightValue.subtract(leftValue);
                if (rightValue.compareTo(SuperNumber.ZERO) == -1) {
                    FabricatedExchange.LOGGER.error("ERROR: Negative EMC value! Recipe: " + recipe.getId().toString());
                } 
                for (Item item : unknownItems) {
                    if (!emcMapHasEntry(item)) {
                        putEmcMap(item, rightValue);
                        newInfo = true;
                    }
                }
            }
            else {
                if (!emcMapHasEntry(outputItem)) {
                    SuperNumber result = equation.getLeft();
                    result.divide(outputStack.getCount());
                    putEmcMap(outputItem, result);
                    newInfo = true;
                }
            }
        }
        return newInfo;
    }


    private boolean iterateSmeltingRecipes(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager, LinkedList<SmeltingRecipe> recipesList) {
        boolean newInfo = false;

                
        int len = recipesList.size();
        for (int i = 0; i < len; i++) {
            SmeltingRecipe recipe = recipesList.get(i);

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
                    FabricatedExchange.LOGGER.warn("WARNING: EMC conflict detected! for recipe " + recipe.getId().toString() + ", for items "  
                    + itemStacks[foundIndex].getName() + " with value " + inEmc.toString() + ", "
                    + itemStacks[j].getName() + " with value " + itemEmc.toString() + ". choosing the lower value."); 
                    inEmc = SuperNumber.min(inEmc, itemEmc);
                }
            }
            Item outputItem = recipe.getOutput(dynamicRegistryManager).getItem();

            SuperNumber outEmc = getItemEmc(outputItem);
           
            if (outEmc.equalsZero()) {
                if (inEmc.equalsZero())
                    continue; // not enough info

                // if in emc is defined but out emc is not
                if (!emcMapHasEntry(outputItem)) {
                    putEmcMap(outputItem, inEmc);
                    newInfo = true;
                }
            }
            else if (inEmc.equalsZero()) {
                 // if out emc is defined but in emc is not
                for (int j = 0; j < itemStacks.length; j++) {
                    Item inputItem = itemStacks[j].getItem();
                    if (!emcMapHasEntry(inputItem)) {
                        putEmcMap(inputItem, outEmc);
                        newInfo = true;
                    }
                }
            }
            else { // we already know these EMC values
                recipesList.remove(i);
                i--;
                len--;
            }

            


        }
        return newInfo;
    }


    private boolean iterateSmithingRecipes(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager, LinkedList<SmithingRecipe> recipesList) {
        boolean newInfo = false;

        int len = recipesList.size();
        for (int i = 0; i < len; i++) {
            // this is deprecated but at the moment the recipe list function gives us LegacySmithingRecipe,
            // when they change it we will properly use SmithingTransformRecipe and SmithingTrimRecipe
            LegacySmithingRecipe recipe = (LegacySmithingRecipe)recipesList.get(i);

            LegacySmithingRecipeAccessor recipeAccessor = (LegacySmithingRecipeAccessor) recipe;
            Ingredient base = recipeAccessor.getBase();
            Ingredient addition = recipeAccessor.getAddition();
            Item a = base.getMatchingStacks()[0].getItem();
            Item b = addition.getMatchingStacks()[0].getItem();
            SuperNumber aEmc = getItemEmc(a);
            SuperNumber bEmc = getItemEmc(b);

            // if outEmc is zero there is not enough info and if it does have a value
            // then we don't know how we should distribute it to the two items
            if (aEmc.equalsZero() || bEmc.equalsZero())
                continue; 


            Item output = recipe.getOutput(dynamicRegistryManager).getItem();
            SuperNumber outEmc = getItemEmc(output);


            if (outEmc.equalsZero()) {
                aEmc.add(bEmc);
                if (!emcMapHasEntry(output)) {
                    putEmcMap(output, aEmc);
                    newInfo = true;
                }
                continue;
            }
            if (aEmc.equalsZero()) {
                if (!emcMapHasEntry(a)) {
                    bEmc.add(outEmc);
                    putEmcMap(a, bEmc);
                }
                continue;
            }
            if (!emcMapHasEntry(b)) {
                aEmc.add(outEmc);
                putEmcMap(b, aEmc);
            }
            
        }
        return newInfo;
    }

    private void putEmcMap(Item item, int value) {
        emcMap.put(Registries.ITEM.getId(item).toString(), new SuperNumber(value));
    }

    private void putEmcMap(Item item, SuperNumber value) {
        emcMap.put(Registries.ITEM.getId(item).toString(), value);
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

}


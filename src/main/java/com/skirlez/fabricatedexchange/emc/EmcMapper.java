package com.skirlez.fabricatedexchange.emc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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
import net.minecraft.world.World;

public class EmcMapper {
    private ConcurrentMap<String, SuperNumber> emcMap;

    private HashSet<String> log;
    private final HashSet<String> modifiers;
    public EmcMapper() {
        emcMap = new ConcurrentHashMap<String, SuperNumber>();
        log = new HashSet<String>();
        modifiers = ModConfig.MODIFIERS.getValue();
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
        This function will fill the EMC map - the thing that sets the relation between items and their emc values.
        this implementation is dumb, because I am not experienced enough to implement a better solution.
        it loops over all the recipes in the game a LOT to generate the mappings.
        */        
        FabricatedExchange.LOGGER.info("Start EMC mapper");
        Map<String, SuperNumber> seedEmcMap = ModConfig.SEED_EMC_MAP_FILE.getValue();
        if (seedEmcMap != null)
            GeneralUtil.mergeMap(emcMap, seedEmcMap);

        // i don't know what this thing is, but you need it for some functions
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();

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
        
        // we must split the recipes between their origin mod. 
        // minecraft recipes should evaluate first, and then we don't actually care about the order.
        HashSet<String> namespaces = new HashSet<String>();

        namespaces.add("minecraft");
        Map<String, LinkedList<SmithingRecipe>> splitSmithingRecipes = splitRecipesToMods(allSmithingRecipes, namespaces, smithingRecipesBlacklist);
        Map<String, LinkedList<SmeltingRecipe>> splitSmeltingRecipes = splitRecipesToMods(allSmeltingRecipes, namespaces, smeltingRecipesBlacklist);
        Map<String, LinkedList<CraftingRecipe>> splitCraftingRecipes = splitRecipesToMods(allCraftingRecipes, namespaces, craftingRecipesBlacklist);
        namespaces.remove("minecraft");
        
        List<String> mods = new ArrayList<String>(namespaces.size() + 1);
        mods.add("minecraft");
        mods.addAll(namespaces);


        for (int m = 0; m < mods.size(); m++) {
            String mod = mods.get(m);
           
            LinkedList<SmithingRecipe> smithingRecipes = splitSmithingRecipes.getOrDefault(mod, null);
            LinkedList<SmeltingRecipe> smeltingRecipes = splitSmeltingRecipes.getOrDefault(mod, null);
            LinkedList<CraftingRecipe> craftingRecipes = splitCraftingRecipes.getOrDefault(mod, null);          

            for (int i = 0; i < 100; i++) {
                // Smithing recipes
                int count = 0;

                if (smithingRecipes == null)
                    count++;
                else for (int j = 0; j < 100; j++) { 
                    if (!iterateSmithingRecipes(recipeManager, dynamicRegistryManager, smithingRecipes)) {
                        if (j == 0)
                            count++;
                        break;
                    }
                }
                // Smelting recipes
                if (smeltingRecipes == null)
                    count++;
                else for (int j = 0; j < 100; j++) { 
                    if (!iterateSmeltingRecipes(recipeManager, dynamicRegistryManager, smeltingRecipes)) {
                        if (j == 0)
                            count++;
                        break;
                    }
                }
                // Crafting recipes
                if (craftingRecipes == null)
                    count++;
                else for (int j = 0; j < 100; j++) {
                    if (!iterateCraftingRecipes(recipeManager, dynamicRegistryManager, craftingRecipes)) {
                        if (j == 0)
                            count++;
                        break;
                    }
                }
                if (count == 3) // if no information was extracted from an iteration
                    break;
            }
        }
        Map<String, SuperNumber> customEmcMap = ModConfig.CUSTOM_EMC_MAP_FILE.getValue();
        if (customEmcMap != null)
            GeneralUtil.mergeMap(EmcData.emcMap, customEmcMap);

        FabricatedExchange.LOGGER.info("End EMC mapper");
    }



    private boolean iterateCraftingRecipes(RecipeManager recipeManager, DynamicRegistryManager dynamicRegistryManager, LinkedList<CraftingRecipe> recipesList) {
        boolean newInfo = false;
        Iterator<CraftingRecipe> iterator = recipesList.iterator();
        while (iterator.hasNext()) {
            CraftingRecipe recipe = iterator.next();
            newInfo = iterateCraftingRecipe(recipe, dynamicRegistryManager) || newInfo;
        }
        return newInfo;
    }
    
    
    private boolean iterateCraftingRecipe(CraftingRecipe recipe, DynamicRegistryManager dynamicRegistryManager) {
        boolean newInfo = false;
        /*
        We will think of every recipe like an algebric equation:
        the input ingredients' EMC values are on the left, and the output EMC value is on the right.
        in this case, one of those might be unknown. We want to move the unknown 
        to the right side of the equation, and numbers to the left.
        This variable represents the left side.
        */


        SuperNumber sum = SuperNumber.Zero();
        
        // The amount of unknown ingredients that are present. If above 1, we give up on this recipe.
        int unknownCount = 0;

        /* We can only "solve" a recipe if it has one unknown ingredient.
        I would have just stored that ingredient, however for multiple reasons we actually need to
        store a list of items that match the ingredient (simplest being that the output is just an itemstack, not an ingredient)
        */
        HashSet<Item> unknownItems = new HashSet<Item>();

        // This value will multiply the unknown's EMC eventually, and could negate it if it moved from left to right
        int unknownMult = 0;

        ItemStack outputStack = recipe.getOutput(dynamicRegistryManager);
        SuperNumber outputEmc = getStackEmc(outputStack);
        if (outputEmc.equalsZero()) {
            // The output is our unknown.
            unknownCount = 1;
            unknownMult = outputStack.getCount();
            unknownItems.add(outputStack.getItem());
        }
        else {
            outputEmc.multiply(outputStack.getCount());
            sum.subtract(outputEmc);
        }
        List<Ingredient> ingredients = new ArrayList<Ingredient>(recipe.getIngredients());
        // First pass over the ingredients: remove any ingredients which are unnecessary
        Iterator<Ingredient> iterator = ingredients.iterator();
        while (iterator.hasNext()) {
            Ingredient ingredient = iterator.next();

            // These are the item stacks which match the ingredient. For example, in the planks to sticks recipe,
            // the two ingredients match every wood plank block.
            ItemStack[] itemStacks = ingredient.getMatchingStacks();

            if (itemStacks.length == 0) { // this will be true for air
                iterator.remove();
                continue;
            }
            for (ItemStack stack : itemStacks) {
                Item item = stack.getItem();
                if (isItemModifier(item)) {
                    iterator.remove();
                    continue;
                }
                if (item.hasRecipeRemainder()) {
                    ItemStack remainder = stack.getRecipeRemainder();
                    if (ItemStack.areEqual(stack, remainder)) {
                        iterator.remove();
                        continue;
                    }
                }
            }
        }

        // this can be true if the recipe was only comprised of modifiers, in which case we can't infer values from it
        if (ingredients.size() == 0)
            return newInfo;
        

        // Second pass over the ingredients: attempt to calculate their EMCs
        for (Ingredient ingredient : ingredients) {

            ItemStack[] itemStacks = ingredient.getMatchingStacks();
            // this number will represent the EMC each matching stack should be worth/what the ingredient itself is worth
            SuperNumber ingredientEmc = SuperNumber.Zero();


            boolean treatAsAir = false;

            // Now we want to check if any of those matching stacks have EMC values
            // if one has, we infer and set all the other matching stacks to have that value as well
            // if not a single one has, we call this ingredient the unknown.
            for (ItemStack stack : itemStacks) {
                Item item = stack.getItem();
                SuperNumber itemEmc = getItemEmc(item);

                if (itemEmc.equalsZero()) { // This item has no known EMC value
                    if (!unknownItems.contains(item)) 
                        continue;
                    // if one of the items in this ingredient is found inside the already unknown items,
                    // we add all the items in this ingredient to the unknown items list.
                    for (ItemStack stack2 : itemStacks) {
                        Item item2 = stack2.getItem(); 
                        // If this is true, this recipe is insanely weird and should be aborted.
                        if (!getItemEmc(item2).equalsZero()) {
                            return newInfo;
                        }

                        if (!unknownItems.contains(item2))
                            unknownItems.add(item2);
                    }

                    // we need to switch sides in the equation, so we subtract one from the unknown(s) on the right
                    unknownMult -= 1;
                    treatAsAir = true;
                    // we can leave the loop from here
                    break;
                }
                if (item.hasRecipeRemainder()) {
                    Item remainder = item.getRecipeRemainder();
                    // If the item has a recipe remainder, we need to subtract its EMC from our item EMC.
                    // If it doesn't have EMC, the item is considered unknown.
                    SuperNumber remainderEmc = getItemEmc(remainder);
                    if (remainderEmc.equalsZero())
                        continue;
   
                    itemEmc.subtract(remainderEmc);
                    /* We need to make a check here for items that have their recipe remainder's EMC value equal
                    to the original item's EMC value. The most common case for this is items that have their recipe remainder as themselves,
                    but it's treated above this, for the case where we don't know the EMC of the item at that point during the mapping.
                    These items count as modifiers and should be removed */
                    int comparison = itemEmc.compareTo(SuperNumber.ZERO);
                    if (comparison == -1) {
                        warn("Negative EMC value upon subtracting recipe remainder! Recipe: " + recipe.getId().toString());
                        break;
                    }
                    if (comparison == 0) {
                        treatAsAir = true;
                        break;
                    }
                }



                // if we made it here, that means one of the items does have EMC.
                ingredientEmc = itemEmc;

                // Now we loop over the stacks again, setting each of their EMC values to the new value.
                for (ItemStack stack2 : itemStacks) {
                    Item item2 = stack2.getItem();
                    if (!item.equals(item2)) 
                        newInfo = putEmcMap(item2, ingredientEmc, recipe) || newInfo;
                }
                break; // we can leave the loop now
            }

            if (treatAsAir) // skip this ingredient
                continue;

            if (ingredientEmc.equalsZero()) {
                if (unknownCount == 0) { 
                    // if there were no unknowns yet, set the count to one, and add all the matching stacks to the unknown items
                    // additionaly set unknownmult to -1 because we're switching sides in the equation
                    for (ItemStack stack : itemStacks) {
                        unknownItems.add(stack.getItem());
                    }
                    unknownCount = 1;
                    unknownMult = -1;
                }
                else {
                    // we have more than one unknown, so we should quit.
                    return newInfo;
                }
            }
            else
                sum.add(ingredientEmc);
        }
        
        if (unknownCount == 0) {
            // if we found no unknowns, run an equality check
            // if the sum is zero, that must mean the equation is correct, since we moved any value from the right side of the equation
            // to the left side, subtracting what's there
            if (!sum.equalsZero()) {
                warn("Inequality detected! Recipe: " + recipe.getId().toString() + " Disparity: " + sum.toString());
            }
            return newInfo;
        }

        // After filling the equation, we now need to solve it.

        // first, if both unknownMult and sum are negative, we can multiply the whole equation by -1, making them positive
        if (sum.isNegative() && unknownMult < 0) {
            sum.negate();
            unknownMult *= -1;
        }
        
        // if one of them is still negative or below 0 after that, something must be wrong
        if (sum.compareTo(SuperNumber.ZERO) <= 0 || unknownMult <= 0) {
            warn("One of the sides in crafting equation is negative or zero! Recipe: " + recipe.getId().toString() + " unknownMult: " + unknownMult + " sum: " + sum.divisionString());
            return newInfo;
        }

        
        SuperNumber unknownWorth = new SuperNumber(sum);
        unknownWorth.divide(unknownMult);
        for (Item item : unknownItems) {
            if (item.hasRecipeRemainder()) {
                SuperNumber remainderEmc = getItemEmc(item.getRecipeRemainder());
                if (remainderEmc.equalsZero())
                    continue;
                remainderEmc.add(unknownWorth);
                newInfo = putEmcMap(item, remainderEmc, recipe) || newInfo;
            }
            else
                newInfo = putEmcMap(item, unknownWorth, recipe) || newInfo;
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
                    warn("Ingredient found with multiple conflicting EMC values! In recipe: " + recipe.getId().toString() + " for items: "  
                    + itemName(itemStacks[foundIndex].getItem()) + " with value " + inEmc + ", "
                    + itemName(itemStacks[j].getItem()) + " with value " + itemEmc
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
            else {
                if (inEmc.equalTo(outEmc)) // we already know both inemc and outemc, run an inequality check
                    iterator.remove();
                else
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
                aEmc.add(bEmc);
                if (!aEmc.equalTo(outEmc))
                    warn("Inequality detected in smithing recipe! Recipe: " + recipe.getId().toString());
                else
                    iterator.remove();
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
            warn("EMC Mapper tried assigning item " + itemName(item) 
            + " a value lower or equal to 0. Current recipe: " + recipe.getId().toString());
            return false;
        }

        if (!emcMapHasEntry(item)) {
            emcMap.put(itemName(item), value);
            return true;
        }

        SuperNumber emc = getItemEmc(item);
        if (emc.equalTo(value))
            return false;
        warn("EMC Conflict for item " + itemName(item) 
        + ", EMC Mapper tried assigning two different values! Original value: " + emc + ", new value: " + value
        + ", current recipe: " + recipe.getId().toString());
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

    private SuperNumber getStackEmc(ItemStack stack) {
        if (stack == null)
            return SuperNumber.Zero(); 
        String id = itemName(stack.getItem());
        if (emcMap.containsKey(id)) {
            SuperNumber emc = new SuperNumber(emcMap.get(id));

            emc.multiply(stack.getCount());
            if (stack.getMaxDamage() != 0) {
                emc.multiply(new SuperNumber(stack.getMaxDamage()-stack.getDamage(), stack.getMaxDamage()));
                emc.floor();
            }
        }
        return SuperNumber.Zero(); 
    }

    private boolean emcMapHasEntry(Item item) {
        return emcMap.containsKey(itemName(item));
    }

    private String itemName(Item item) {
        return Registries.ITEM.getId(item).toString();
    }

    private void warn(String input) {
        if (!log.contains(input)) {
            FabricatedExchange.LOGGER.warn(input);
            log.add(input);
        }
    }

    private boolean isItemModifier(Item item) {
        if (modifiers.contains(itemName(item)))
            return true;
        List<String> tags = Registries.ITEM.getEntry(item).streamTags().map((key) -> ("#" + key.id().toString())).collect(Collectors.toList());
        for (int i = 0; i < tags.size(); i++) {
            if (modifiers.contains(tags.get(i)))
                return true;
        }
        return false;
    }

    private <T extends Recipe<?>> Map<String, LinkedList<T>> splitRecipesToMods(List<T> allRecipes, HashSet<String> namespaces, HashSet<String> blacklist) {
        Map<String, LinkedList<T>> splitRecipes = new HashMap<String, LinkedList<T>>();
        for (int i = 0; i < allRecipes.size(); i++) {
            T recipe = allRecipes.get(i);
            if (blacklist.contains(recipe.getId().toString()))
                continue;
            String namespace = recipe.getId().getNamespace();
            if (!namespaces.contains(namespace))
                namespaces.add(namespace);
            if (!splitRecipes.containsKey(namespace)) {
                LinkedList<T> newList = new LinkedList<T>();
                newList.add(recipe);
                splitRecipes.put(namespace, newList);
            }
            else
                splitRecipes.get(namespace).add(recipe);
        }
        return splitRecipes;
    }

}


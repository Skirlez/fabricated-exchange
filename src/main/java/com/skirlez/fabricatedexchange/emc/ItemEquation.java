package com.skirlez.fabricatedexchange.emc;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

/** This class represents an equality of items: an amount of input ingredients equals a list of outputs. It should be able to represent
 * most if not all vanilla and custom recipes.
 */
public class ItemEquation {
    public final List<Ingredient> input;
    public final List<ItemStack> output;

    // The EMC mapper will output this name when warning about a recipe
    public final String name;

    public int amountUnknown;

    public ItemEquation(List<Ingredient> input, List<ItemStack> output, String name) {
        this.input = input;
        this.output = output;
        this.name = name;
        this.amountUnknown = 0;
    }
}

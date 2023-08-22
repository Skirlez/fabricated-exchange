package com.skirlez.fabricatedexchange.emc;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;

/** This class represents an equality of items: an amount of input ingredients equals a list of outputs. It should be able to represent
 * most if not all vanilla and custom recipes.
 */
public class ItemEquation {
    public final List<Ingredient> input;
    public final List<ItemStack> output;

    // The EMC mapper will output this name when warning about a recipe
    public final String name;
    public final String origin;

    public int amountUnknown;

    public ItemEquation(List<Ingredient> input, List<ItemStack> output, Identifier id) {
        this.input = input;
        this.output = output;
        this.name = id.getPath();
        this.origin = id.getNamespace();
        this.amountUnknown = 0;
    }
}

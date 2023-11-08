package com.skirlez.fabricatedexchange.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class RedMatterMaterial implements ToolMaterial {

    public static final RedMatterMaterial INSTANCE = new RedMatterMaterial();

    private RedMatterMaterial() {

    }

    @Override
    public int getDurability() {
        return 0;
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 20f;
    }

    @Override
    public float getAttackDamage() {
        return 0f;
    }

    @Override
    public int getMiningLevel() { return 4; }

    @Override
    public int getEnchantability() {
        return 0;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(ModItems.DARK_MATTER);
    }
    
}

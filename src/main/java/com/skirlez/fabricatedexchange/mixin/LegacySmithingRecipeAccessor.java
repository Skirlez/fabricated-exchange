package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.LegacySmithingRecipe;

@Mixin(LegacySmithingRecipe.class)
public interface LegacySmithingRecipeAccessor {
    @Accessor
    Ingredient getAddition();
    @Accessor
    Ingredient getBase();
}
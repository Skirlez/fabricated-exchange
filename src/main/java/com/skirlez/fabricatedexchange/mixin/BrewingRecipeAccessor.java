package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;

@Mixin(BrewingRecipeRegistry.Recipe.class)
public interface BrewingRecipeAccessor<T>  {
    @Accessor
    public T getInput();
    @Accessor
    public Ingredient getIngredient();
    @Accessor
    public T getOutput();
}

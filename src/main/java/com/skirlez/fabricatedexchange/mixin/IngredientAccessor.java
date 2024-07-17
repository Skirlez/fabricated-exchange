package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.Ingredient;

@Mixin(Ingredient.class)
public interface IngredientAccessor  {
	@Accessor
	public Ingredient.Entry[] getEntries();

}

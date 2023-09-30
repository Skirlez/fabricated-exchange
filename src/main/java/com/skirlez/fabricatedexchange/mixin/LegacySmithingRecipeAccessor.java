package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.*;

@SuppressWarnings("removal")
@Mixin(LegacySmithingRecipe.class)
public interface LegacySmithingRecipeAccessor {
	@Accessor
	Ingredient getAddition();
	@Accessor
	Ingredient getBase();
}
package com.skirlez.fabricatedexchange.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.BrewingRecipeRegistry;


@Mixin(BrewingRecipeRegistry.class)
public interface BrewingRecipeRegistryAccessor {
	@Accessor(value = "POTION_RECIPES")
	public static List<BrewingRecipeRegistry.Recipe<Potion>> getPotionRecipes() {
		return null;
	}
	@Accessor(value = "ITEM_RECIPES")
	public static List<BrewingRecipeRegistry.Recipe<Item>> getItemRecipes() {
		return null;
	}
	
}
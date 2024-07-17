package com.skirlez.fabricatedexchange.item;

import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public abstract class ModToolMaterials {
	public static final ToolMaterial DARK_MATTER_MATERIAL = new ToolMaterial() {
		@Override
		public int getDurability() {
			return 0;
		}

		@Override
		public float getMiningSpeedMultiplier() {
			return 10f;
		}

		@Override
		public float getAttackDamage() {
			return 0f;
		}

		@Override
		public int getMiningLevel() {
			return 4;
		}

		@Override
		public int getEnchantability() {
			return 0;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(ModItems.DARK_MATTER);
		}
	};
	
	public static final ToolMaterial RED_MATTER_MATERIAL = new ToolMaterial() {
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
		public int getMiningLevel() { 
			return 4;
		}

		@Override
		public int getEnchantability() {
			return 0;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(ModItems.RED_MATTER);
		}
	};
}

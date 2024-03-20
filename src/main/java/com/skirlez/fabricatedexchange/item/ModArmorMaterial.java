package com.skirlez.fabricatedexchange.item;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorItem.Type;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public final class ModArmorMaterial {
	private ModArmorMaterial() {
	}
	
	public static ArmorMaterial DARK_MATTER = new ArmorMaterial() {
		private static final int[] PROTECTION_VALUES = new int[] {3, 6, 8, 3};
		
		@Override
		public float getToughness() {
			return 2f;
		}
		
		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(ModItems.DARK_MATTER);
		}
		
		@Override
		public int getProtection(Type type) {
			return PROTECTION_VALUES[type.ordinal()];
		}
		
		@Override
		public String getName() {
			return "dark_matter";
		}
		
		@Override
		public float getKnockbackResistance() {
			return 0.1f;
		}
		
		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
		}
		
		@Override
		public int getEnchantability() {
			return 0;
		}
		
		@Override
		public int getDurability(Type type) {
			return 0;
		}
	};
	public static ArmorMaterial RED_MATTER = new ArmorMaterial() {
		private static final int[] PROTECTION_VALUES = new int[] {3, 6, 8, 3};
		
		@Override
		public float getToughness() {
			return 2f;
		}
		
		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(ModItems.RED_MATTER);
		}
		
		@Override
		public int getProtection(Type type) {
			return PROTECTION_VALUES[type.ordinal()];
		}
		
		@Override
		public String getName() {
			return "red_matter";
		}
		
		@Override
		public float getKnockbackResistance() {
			return 0.2f;
		}
		
		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND;
		}
		
		@Override
		public int getEnchantability() {
			return 0;
		}
		
		@Override
		public int getDurability(Type type) {
			return 0;
		}
	};
	
}

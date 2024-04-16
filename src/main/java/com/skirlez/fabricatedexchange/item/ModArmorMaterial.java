package com.skirlez.fabricatedexchange.item;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public final class ModArmorMaterial {
	private ModArmorMaterial() {
	}
	
	public static ArmorMaterial DARK_MATTER = new ArmorMaterial() {
		@Override
		public float getToughness() {
			return 2f;
		}
		
		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(ModItems.DARK_MATTER);
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
		public int getDurability(EquipmentSlot slot) {
			return 0;
		}

		@Override
		public int getProtectionAmount(EquipmentSlot slot) {
			return switch (slot) {
				case FEET -> 3;
				case LEGS -> 6;
				case CHEST -> 8;
				case HEAD -> 3;
				default -> 0;
			};
		}
	};
	public static ArmorMaterial RED_MATTER = new ArmorMaterial() {
		@Override
		public float getToughness() {
			return 2f;
		}
		
		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(ModItems.RED_MATTER);
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
		public int getDurability(EquipmentSlot slot) {
			return 0;
		}

		@Override
		public int getProtectionAmount(EquipmentSlot slot) {
			return switch (slot) {
				case FEET -> 3;
				case LEGS -> 6;
				case CHEST -> 8;
				case HEAD -> 3;
				default -> 0;
			};
		}
	};
	
}

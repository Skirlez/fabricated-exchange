package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.extras.ItemOrb;
import com.skirlez.fabricatedexchange.item.rings.ArchangelsSmite;
import com.skirlez.fabricatedexchange.item.rings.SwiftWolfsRendingGale;
import com.skirlez.fabricatedexchange.item.tools.*;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {
	public static final Item PHILOSOPHERS_STONE = registerItem("philosophers_stone",
			new PhilosophersStone(new FabricItemSettings().maxCount(1).group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static final Item TRANSMUTATION_TABLET = registerItem("transmutation_tablet",
			new TransmutationTablet(new FabricItemSettings().maxCount(1).group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item ALCHEMICAL_COAL = registerItem("alchemical_coal",
			new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item RADIANT_COAL = registerItem("radiant_coal", new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item MOBIUS_FUEL = registerItem("mobius_fuel", new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item AETERNALIS_FUEL = registerItem("aeternalis_fuel",
			new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item LOW_COVALENCE_DUST = registerItem("low_covalence_dust",
			new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item MEDIUM_COVALENCE_DUST = registerItem("medium_covalence_dust",
			new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item HIGH_COVALENCE_DUST = registerItem("high_covalence_dust",
			new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item IRON_BAND = registerItem("iron_band", new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item DARK_MATTER = registerItem("dark_matter", new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item RED_MATTER = registerItem("red_matter", new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item TOME_OF_KNOWLEDGE = registerItem("tome_of_knowledge",
			new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static final Item ITEM_ORB = registerItem("item_orb", new ItemOrb(new FabricItemSettings().maxCount(1)));

	public static final Item DARK_MATTER_SWORD = registerItem("dark_matter_sword",
			new MatterSword(ModToolMaterials.DARK_MATTER_MATERIAL, 12, -2.4f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item DARK_MATTER_PICKAXE = registerItem("dark_matter_pickaxe",
			new MatterPickaxe(ModToolMaterials.DARK_MATTER_MATERIAL, 7, -2.8f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item DARK_MATTER_SHOVEL = registerItem("dark_matter_shovel",
			new MatterShovel(ModToolMaterials.DARK_MATTER_MATERIAL, 5f, -3.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item DARK_MATTER_AXE = registerItem("dark_matter_axe",
			new MatterAxe(ModToolMaterials.DARK_MATTER_MATERIAL, 8, -3.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item DARK_MATTER_HOE = registerItem("dark_matter_hoe",
			new MatterHoe(ModToolMaterials.DARK_MATTER_MATERIAL, 5, 0.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static final Item DARK_MATTER_HAMMER = registerItem("dark_matter_hammer",
			new DarkMatterHammer(ModToolMaterials.DARK_MATTER_MATERIAL, 7, -2.8f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static final Item RED_MATTER_SWORD = registerItem("red_matter_sword",
			new MatterSword(ModToolMaterials.RED_MATTER_MATERIAL, 14, -2.4f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static final Item RED_MATTER_PICKAXE = registerItem("red_matter_pickaxe",
			new MatterPickaxe(ModToolMaterials.RED_MATTER_MATERIAL, 10, -2.8f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item RED_MATTER_SHOVEL = registerItem("red_matter_shovel",
			new MatterShovel(ModToolMaterials.RED_MATTER_MATERIAL, 7, -3.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item RED_MATTER_AXE = registerItem("red_matter_axe",
			new MatterAxe(ModToolMaterials.RED_MATTER_MATERIAL, 12, -3.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item RED_MATTER_HOE = registerItem("red_matter_hoe",
			new MatterHoe(ModToolMaterials.RED_MATTER_MATERIAL, 8, 0.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static final Item RED_MATTER_HAMMER = registerItem("red_matter_hammer",
			new RedMatterHammer(ModToolMaterials.RED_MATTER_MATERIAL, 10, -2.8f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static Item DARK_MATTER_BOOTS = registerItem("dark_matter_boots",
			new ArmorItem(ModArmorMaterial.DARK_MATTER, EquipmentSlot.FEET, new Item.Settings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static Item DARK_MATTER_LEGGINGS = registerItem("dark_matter_leggings",
			new ArmorItem(ModArmorMaterial.DARK_MATTER, EquipmentSlot.LEGS, new Item.Settings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static Item DARK_MATTER_CHESTPLATE = registerItem("dark_matter_chestplate",
			new ArmorItem(ModArmorMaterial.DARK_MATTER, EquipmentSlot.CHEST, new Item.Settings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static Item DARK_MATTER_HELMET = registerItem("dark_matter_helmet",
			new ArmorItem(ModArmorMaterial.DARK_MATTER, EquipmentSlot.HEAD, new Item.Settings().group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static Item RED_MATTER_BOOTS = registerItem("red_matter_boots",
			new ArmorItem(ModArmorMaterial.RED_MATTER, EquipmentSlot.FEET, new Item.Settings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static Item RED_MATTER_LEGGINGS = registerItem("red_matter_leggings",
			new ArmorItem(ModArmorMaterial.RED_MATTER, EquipmentSlot.LEGS, new Item.Settings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static Item RED_MATTER_CHESTPLATE = registerItem("red_matter_chestplate",
			new ArmorItem(ModArmorMaterial.RED_MATTER, EquipmentSlot.CHEST, new Item.Settings().group(ModItemGroups.FABRICATED_EXCHANGE)));
	public static Item RED_MATTER_HELMET = registerItem("red_matter_helmet",
			new ArmorItem(ModArmorMaterial.RED_MATTER, EquipmentSlot.HEAD, new Item.Settings().group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static final Item SWIFTWOLFS_RENDING_GALE = registerItem("swiftwolfs_rending_gale",
			new SwiftWolfsRendingGale(new Item.Settings().maxCount(1).group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static final Item ARCHANGELS_SMITE = registerItem("archangels_smite",
			new ArchangelsSmite(new Item.Settings().maxCount(1).group(ModItemGroups.FABRICATED_EXCHANGE)));

	public static final Item REPAIR_TALISMAN = registerItem("repair_talisman",
			new RepairTalisman(new Item.Settings().maxCount(1).group(ModItemGroups.FABRICATED_EXCHANGE)));

	private static Item registerItem(String name, Item item) {
		Registry.register(Registry.ITEM, new Identifier(FabricatedExchange.MOD_ID, name), item);
		return item;
	}

	public static void registerModItems() {
	}
}

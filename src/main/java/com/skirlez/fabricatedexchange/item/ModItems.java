package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.extras.ItemOrb;
import com.skirlez.fabricatedexchange.item.rings.*;
import com.skirlez.fabricatedexchange.item.stones.GemOfEternalDensity;
import com.skirlez.fabricatedexchange.item.tools.*;

import com.skirlez.fabricatedexchange.item.tools.DiviningRod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class ModItems {
	public static final Item PHILOSOPHERS_STONE = registerItem("philosophers_stone",
			new PhilosophersStone(new FabricItemSettings().maxCount(1)));

	public static final Item TRANSMUTATION_TABLET = registerItemWithGroup("transmutation_tablet",
			new TransmutationTablet(new FabricItemSettings().maxCount(1)));
	public static final Item ALCHEMICAL_COAL = registerItemWithGroup("alchemical_coal",
			new Item(new FabricItemSettings()));
	public static final Item RADIANT_COAL = registerItemWithGroup("radiant_coal", new Item(new FabricItemSettings()));
	public static final Item MOBIUS_FUEL = registerItemWithGroup("mobius_fuel", new Item(new FabricItemSettings()));
	public static final Item AETERNALIS_FUEL = registerItemWithGroup("aeternalis_fuel",
			new Item(new FabricItemSettings()));
	public static final Item LOW_COVALENCE_DUST = registerItemWithGroup("low_covalence_dust",
			new Item(new FabricItemSettings()));
	public static final Item MEDIUM_COVALENCE_DUST = registerItemWithGroup("medium_covalence_dust",
			new Item(new FabricItemSettings()));
	public static final Item HIGH_COVALENCE_DUST = registerItemWithGroup("high_covalence_dust",
			new Item(new FabricItemSettings()));
	
	public static final Item LOW_DIVIDING_ROD = registerItemWithGroup("low_divining_rod",
			new DiviningRod(new FabricItemSettings().maxCount(1), 3));
	public static final Item MEDIUM_DIVIDING_ROD = registerItemWithGroup("medium_divining_rod",
			new DiviningRod(new FabricItemSettings().maxCount(1), 16));
	public static final Item HIGH_DIVIDING_ROD = registerItemWithGroup("high_divining_rod",
			new DiviningRod(new FabricItemSettings().maxCount(1), 64));
	
	public static final Item IRON_BAND = registerItemWithGroup("iron_band", new Item(new FabricItemSettings()));
	public static final Item DARK_MATTER = registerItemWithGroup("dark_matter", new Item(new FabricItemSettings()));
	public static final Item RED_MATTER = registerItemWithGroup("red_matter", new Item(new FabricItemSettings()));
	public static final Item TOME_OF_KNOWLEDGE = registerItemWithGroup("tome_of_knowledge",
			new Item(new FabricItemSettings()));

	public static final Item ITEM_ORB = registerItem("item_orb", new ItemOrb(new FabricItemSettings().maxCount(1)));
	public static final Item WATER_ORB = registerItem("water_orb", new Item(new FabricItemSettings().maxCount(1)));
	public static final Item LAVA_ORB = registerItem("lava_orb", new Item(new FabricItemSettings().maxCount(1)));
	public static final Item TORNADO_ORB = registerItem("tornado_orb", new Item(new FabricItemSettings().maxCount(1)));

	public static final Item FROZEN_ORB = registerItem("frozen_orb", new Item(new FabricItemSettings().maxCount(1)));

	public static final Item DARK_MATTER_SWORD = registerItemWithGroup("dark_matter_sword",
			new MatterSword(ModToolMaterials.DARK_MATTER_MATERIAL, 12, -2.4f, new Item.Settings().fireproof()));
	public static final Item DARK_MATTER_PICKAXE = registerItemWithGroup("dark_matter_pickaxe",
			new MatterPickaxe(ModToolMaterials.DARK_MATTER_MATERIAL, 7, -2.8f, new Item.Settings().fireproof()));
	public static final Item DARK_MATTER_SHOVEL = registerItemWithGroup("dark_matter_shovel",
			new MatterShovel(ModToolMaterials.DARK_MATTER_MATERIAL, 5f, -3.0f, new Item.Settings().fireproof()));
	public static final Item DARK_MATTER_AXE = registerItemWithGroup("dark_matter_axe",
			new MatterAxe(ModToolMaterials.DARK_MATTER_MATERIAL, 8, -3.0f, new Item.Settings().fireproof()));
	public static final Item DARK_MATTER_HOE = registerItemWithGroup("dark_matter_hoe",
			new MatterHoe(ModToolMaterials.DARK_MATTER_MATERIAL, 5, 0.0f, new Item.Settings().fireproof()));

	public static final Item DARK_MATTER_HAMMER = registerItemWithGroup("dark_matter_hammer",
			new DarkMatterHammer(ModToolMaterials.DARK_MATTER_MATERIAL, 7, -2.8f, new Item.Settings().fireproof()));

	public static final Item RED_MATTER_SWORD = registerItemWithGroup("red_matter_sword",
			new MatterSword(ModToolMaterials.RED_MATTER_MATERIAL, 14, -2.4f, new Item.Settings().fireproof()));

	public static final Item RED_MATTER_PICKAXE = registerItemWithGroup("red_matter_pickaxe",
			new MatterPickaxe(ModToolMaterials.RED_MATTER_MATERIAL, 10, -2.8f, new Item.Settings().fireproof()));
	public static final Item RED_MATTER_SHOVEL = registerItemWithGroup("red_matter_shovel",
			new MatterShovel(ModToolMaterials.RED_MATTER_MATERIAL, 7, -3.0f, new Item.Settings().fireproof()));
	public static final Item RED_MATTER_AXE = registerItemWithGroup("red_matter_axe",
			new MatterAxe(ModToolMaterials.RED_MATTER_MATERIAL, 12, -3.0f, new Item.Settings().fireproof()));
	public static final Item RED_MATTER_HOE = registerItemWithGroup("red_matter_hoe",
			new MatterHoe(ModToolMaterials.RED_MATTER_MATERIAL, 8, 0.0f, new Item.Settings().fireproof()));
	public static final Item RED_MATTER_HAMMER = registerItemWithGroup("red_matter_hammer",
			new RedMatterHammer(ModToolMaterials.RED_MATTER_MATERIAL, 10, -2.8f, new Item.Settings().fireproof()));

	public static Item DARK_MATTER_BOOTS = registerItemWithGroup("dark_matter_boots",
			new ArmorItem(ModArmorMaterial.DARK_MATTER, ArmorItem.Type.BOOTS, new Item.Settings()));
	public static Item DARK_MATTER_LEGGINGS = registerItemWithGroup("dark_matter_leggings",
			new ArmorItem(ModArmorMaterial.DARK_MATTER, ArmorItem.Type.LEGGINGS, new Item.Settings()));
	public static Item DARK_MATTER_CHESTPLATE = registerItemWithGroup("dark_matter_chestplate",
			new ArmorItem(ModArmorMaterial.DARK_MATTER, ArmorItem.Type.CHESTPLATE, new Item.Settings()));
	public static Item DARK_MATTER_HELMET = registerItemWithGroup("dark_matter_helmet",
			new ArmorItem(ModArmorMaterial.DARK_MATTER, ArmorItem.Type.HELMET, new Item.Settings()));

	public static Item RED_MATTER_BOOTS = registerItemWithGroup("red_matter_boots",
			new ArmorItem(ModArmorMaterial.RED_MATTER, ArmorItem.Type.BOOTS, new Item.Settings()));
	public static Item RED_MATTER_LEGGINGS = registerItemWithGroup("red_matter_leggings",
			new ArmorItem(ModArmorMaterial.RED_MATTER, ArmorItem.Type.LEGGINGS, new Item.Settings()));
	public static Item RED_MATTER_CHESTPLATE = registerItemWithGroup("red_matter_chestplate",
			new ArmorItem(ModArmorMaterial.RED_MATTER, ArmorItem.Type.CHESTPLATE, new Item.Settings()));
	public static Item RED_MATTER_HELMET = registerItemWithGroup("red_matter_helmet",
			new ArmorItem(ModArmorMaterial.RED_MATTER, ArmorItem.Type.HELMET, new Item.Settings()));

	public static final Item SWIFTWOLFS_RENDING_GALE = registerItemWithGroup("swiftwolfs_rending_gale",
			new SwiftWolfsRendingGale(new FabricItemSettings().maxCount(1)));

	public static final Item REPAIR_TALISMAN = registerItemWithGroup("repair_talisman",
			new RepairTalisman(new FabricItemSettings().maxCount(1)));

	public static final Item GEM_OF_ETERNAL_DENSITY = registerItemWithGroup("gem_of_eternal_density",
			new GemOfEternalDensity(new FabricItemSettings().maxCount(1)));

	public static final Item BLACK_HOLE_BAND = registerItemWithGroup("black_hole_band",
			new BlackHoleBand(new FabricItemSettings().maxCount(1)));

	public static final Item ARCHANGELS_SMITE = registerItemWithGroup("archangels_smite",
		new ArchangelsSmite(new FabricItemSettings().maxCount(1)));

	public static final Item IGNITION_RING = registerItemWithGroup("ignition_ring",
			new IgnitionRing(new FabricItemSettings().maxCount(1)));

	public static final Item HYDRATION_RING = registerItemWithGroup("hydration_ring",
			new HydrationRing(new FabricItemSettings().maxCount(1)));

	public static final Item ZERO_RING = registerItemWithGroup("zero_ring",
			new ZeroRing(new FabricItemSettings().maxCount(1)));

	public static final Item EVERTIDE_AMULET = registerItemWithGroup("evertide_amulet",
			new EvertideAmulet(new FabricItemSettings().maxCount(1)));

	public static final Item VOLCANITE_AMULET = registerItemWithGroup("volcanite_amulet",
			new VolcaniteAmulet(new FabricItemSettings().maxCount(1)));

	private static Item registerItem(String name, Item item) {
		Registry.register(Registries.ITEM, new Identifier(FabricatedExchange.MOD_ID, name), item);
		return item;
	}

	private static Item registerGroup(Item item) {
		ItemGroupEvents.modifyEntriesEvent(ModItemGroups.FABRICATED_EXCHANGE).register(entries -> entries.add(item));
		return item;
	}

	private static Item registerItemWithGroup(String name, Item item) {
		registerItem(name, item);
		registerGroup(item);
		return item;
	}

	public static void registerModItems() {
		registerGroup(PHILOSOPHERS_STONE);
	}
}

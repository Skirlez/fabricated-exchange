package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.extras.ItemOrb;
import com.skirlez.fabricatedexchange.item.rings.SwiftWolfsRendingGale;
import com.skirlez.fabricatedexchange.item.tools.*;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
	public static final Item RADIANT_COAL = registerItemWithGroup("radiant_coal", 
		new Item(new FabricItemSettings()));
	public static final Item MOBIUS_FUEL = registerItemWithGroup("mobius_fuel", 
		new Item(new FabricItemSettings()));
	public static final Item AETERNALIS_FUEL = registerItemWithGroup("aeternalis_fuel", 
		new Item(new FabricItemSettings()));
	public static final Item LOW_COVALENCE_DUST = registerItemWithGroup("low_covalence_dust", 
		new Item(new FabricItemSettings()));
	public static final Item MEDIUM_COVALENCE_DUST = registerItemWithGroup("medium_covalence_dust", 
		new Item(new FabricItemSettings()));
	public static final Item HIGH_COVALENCE_DUST = registerItemWithGroup("high_covalence_dust", 
		new Item(new FabricItemSettings()));
	public static final Item IRON_BAND = registerItemWithGroup("iron_band",
		new Item(new FabricItemSettings()));
	public static final Item DARK_MATTER = registerItemWithGroup("dark_matter", 
		new Item(new FabricItemSettings()));
	public static final Item RED_MATTER = registerItemWithGroup("red_matter", 
		new Item(new FabricItemSettings()));	
	public static final Item TOME_OF_KNOWLEDGE = registerItemWithGroup("tome_of_knowledge", 
		new Item(new FabricItemSettings()));

	public static final Item ITEM_ORB = registerItemWithGroup("item_orb",
			new ItemOrb(new FabricItemSettings().maxCount(1)));

	public static final Item DARK_MATTER_SWORD = 
	   registerItemWithGroup("dark_matter_sword", new DarkMatterSword(DarkMatterMaterial.INSTANCE, 12, -2.4f, new Item.Settings().fireproof()));
	public static final Item DARK_MATTER_PICKAXE = 
		registerItemWithGroup("dark_matter_pickaxe", new DarkMatterPickaxe(DarkMatterMaterial.INSTANCE, 7, -2.8f, new Item.Settings().fireproof()));
	public static final Item DARK_MATTER_SHOVEL = 
		registerItemWithGroup("dark_matter_shovel", new DarkMatterShovel(DarkMatterMaterial.INSTANCE, 5f, -3.0f, new Item.Settings().fireproof()));
	public static final Item DARK_MATTER_AXE = 
		registerItemWithGroup("dark_matter_axe", new DarkMatterAxe(DarkMatterMaterial.INSTANCE, 8f, -3.0f, new Item.Settings().fireproof()));
	public static final Item DARK_MATTER_HOE = 
		registerItemWithGroup("dark_matter_hoe", new DarkMatterHoe(DarkMatterMaterial.INSTANCE, 5, 0.0f, new Item.Settings().fireproof()));

	public static final Item DARK_MATTER_HAMMER =
			registerItemWithGroup("dark_matter_hammer", new DarkMatterHammer(DarkMatterMaterial.INSTANCE, 7, -2.8f, new Item.Settings().fireproof()));

    public static final Item RED_MATTER_SWORD =
			registerItemWithGroup("red_matter_sword", new RedMatterSword(RedMatterMaterial.INSTANCE, 14, -2.4f, new Item.Settings().fireproof()));

	public static final Item RED_MATTER_PICKAXE =
			registerItemWithGroup("red_matter_pickaxe", new RedMatterPickaxe(RedMatterMaterial.INSTANCE, 10, -2.8f, new Item.Settings().fireproof()));
	public static final Item RED_MATTER_SHOVEL =
			registerItemWithGroup("red_matter_shovel", new RedMatterShovel(DarkMatterMaterial.INSTANCE, 7f, -3.0f, new Item.Settings().fireproof()));
	public static final Item RED_MATTER_AXE =
			registerItemWithGroup("red_matter_axe", new RedMatterAxe(DarkMatterMaterial.INSTANCE, 12f, -3.0f, new Item.Settings().fireproof()));
	public static final Item RED_MATTER_HOE =
			registerItemWithGroup("red_matter_hoe", new RedMatterHoe(DarkMatterMaterial.INSTANCE, 8, 0.0f, new Item.Settings().fireproof()));

	public static final Item RED_MATTER_HAMMER =
			registerItemWithGroup("red_matter_hammer", new RedMatterHammer(RedMatterMaterial.INSTANCE, 10, -2.8f, new Item.Settings().fireproof()));

	public static final Item SWIFTWOLFS_RENDING_GALE = registerItemWithGroup("swiftwolfs_rending_gale",
			new SwiftWolfsRendingGale(new FabricItemSettings().maxCount(1)));

	private static Item registerItem(String name, Item item) {
		Registry.register(Registries.ITEM, new Identifier(FabricatedExchange.MOD_ID, name), item);
		return item;
	}
	private static Item registerGroup(Item item) {
		ItemGroupEvents.modifyEntriesEvent(ModItemGroups.FABRICATED_EXCHANGE).register(entries->entries.add(item));
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

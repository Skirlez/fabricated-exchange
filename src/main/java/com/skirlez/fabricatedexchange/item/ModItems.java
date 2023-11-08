package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.tools.*;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
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
    public static final Item RADIANT_COAL = registerItem("radiant_coal", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item MOBIUS_FUEL = registerItem("mobius_fuel", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item AETERNALIS_FUEL = registerItem("aeternalis_fuel", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item LOW_COVALENCE_DUST = registerItem("low_covalence_dust", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item MEDIUM_COVALENCE_DUST = registerItem("medium_covalence_dust", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item HIGH_COVALENCE_DUST = registerItem("high_covalence_dust", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item DARK_MATTER = registerItem("dark_matter", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item RED_MATTER = registerItem("red_matter", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));    
    public static final Item TOME_OF_KNOWLEDGE = registerItem("tome_of_knowledge", 
        new Item(new FabricItemSettings().group(ModItemGroups.FABRICATED_EXCHANGE)));   

    public static final Item DARK_MATTER_SWORD = 
       registerItem("dark_matter_sword", new DarkMatterSword(DarkMatterMaterial.INSTANCE, 12, -2.4f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item DARK_MATTER_PICKAXE = 
        registerItem("dark_matter_pickaxe", new DarkMatterPickaxe(DarkMatterMaterial.INSTANCE, 7, -2.8f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item DARK_MATTER_SHOVEL = 
        registerItem("dark_matter_shovel", new DarkMatterShovel(DarkMatterMaterial.INSTANCE, 5f, -3.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item DARK_MATTER_AXE = 
        registerItem("dark_matter_axe", new DarkMatterAxe(DarkMatterMaterial.INSTANCE, 8f, -3.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));
    public static final Item DARK_MATTER_HOE = 
        registerItem("dark_matter_hoe", new DarkMatterHoe(DarkMatterMaterial.INSTANCE, 5, 0.0f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));

    public static final Item RED_MATTER_SWORD =
            registerItem("red_matter_sword", new RedMatterSword(RedMatterMaterial.INSTANCE, 14, -2.4f, new Item.Settings().fireproof().group(ModItemGroups.FABRICATED_EXCHANGE)));

    private static Item registerItem(String name, Item item) {
        Registry.register(Registry.ITEM, new Identifier(FabricatedExchange.MOD_ID, name), item);
        return item;
    }
    public static void registerModItems() { 
    }   
}

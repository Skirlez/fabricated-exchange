package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.ModItemGroups;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block ALCHEMICAL_COAL_BLOCK = registerBlock("alchemical_coal_block", 
        new Block(FabricBlockSettings.of(Material.STONE, MapColor.RED).requiresTool().strength(5.0f, 6.0f)));
    public static final Block MOBIUS_FUEL_BLOCK = registerBlock("mobius_fuel_block", 
        new Block(FabricBlockSettings.of(Material.STONE, MapColor.RED).requiresTool().strength(5.0f, 6.0f)));
    public static final Block AETERNALIS_FUEL_BLOCK = registerBlock("aeternalis_fuel_block", 
        new Block(FabricBlockSettings.of(Material.STONE, MapColor.WHITE).requiresTool().strength(5.0f, 6.0f)));

    public static final Block TRANSMUTATION_TABLE = registerBlock("transmutation_table", 
        new TransmutationTable(FabricBlockSettings.of(Material.STONE, MapColor.WHITE)));

    public static final Block ENERGY_COLLECTOR_MK1 = registerBlock("energy_collector_mk1", 
        new EnergyCollector(FabricBlockSettings.of(Material.GLASS, MapColor.PALE_YELLOW).requiresTool().strength(5.0f, 6.0f).luminance(state -> 5), 0));
    public static final Block ENERGY_COLLECTOR_MK2 = registerBlock("energy_collector_mk2", 
        new EnergyCollector(FabricBlockSettings.of(Material.GLASS, MapColor.PALE_YELLOW).requiresTool().strength(5.0f, 6.0f).luminance(state -> 10), 1));
    public static final Block ENERGY_COLLECTOR_MK3 = registerBlock("energy_collector_mk3", 
        new EnergyCollector(FabricBlockSettings.of(Material.GLASS, MapColor.PALE_YELLOW).requiresTool().strength(5.0f, 6.0f).luminance(state -> 15), 2));

    public static final Block ANTIMATTER_RELAY_MK1 = registerBlock("antimatter_relay_mk1", 
        new AntiMatterRelay(FabricBlockSettings.of(Material.GLASS, MapColor.BLACK).requiresTool().strength(3.5f), 0));
    public static final Block ANTIMATTER_RELAY_MK2 = registerBlock("antimatter_relay_mk2", 
        new AntiMatterRelay(FabricBlockSettings.of(Material.GLASS, MapColor.BLACK).requiresTool().strength(3.5f), 1));
    public static final Block ANTIMATTER_RELAY_MK3 = registerBlock("antimatter_relay_mk3", 
        new AntiMatterRelay(FabricBlockSettings.of(Material.GLASS, MapColor.BLACK).requiresTool().strength(3.5f), 2));

    public static final Block ALCHEMICAL_CHEST = registerBlock("alchemical_chest", 
        new AlchemicalChest(FabricBlockSettings.of(Material.GLASS, MapColor.BLACK).requiresTool().strength(3.5f)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(FabricatedExchange.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        Item item = Registry.register(Registries.ITEM, new Identifier(FabricatedExchange.MOD_ID, name),
        new BlockItem(block, new FabricItemSettings()));
        ItemGroupEvents.modifyEntriesEvent(ModItemGroups.FABRICATED_EXCHANGE).register(entries -> entries.add(item));
        return item;
    }

    public static void registerModBlocks() {

    }
}

package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;


public class ModItems {
    
    public static final Item PHILOSOPHERS_STONE = registerItem("philosophers_stone", 
        new PhilosophersStone(new FabricItemSettings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(FabricatedExchange.MOD_ID, name), item);
    }

    public static void addItemsToItemGroup() {
        addToItemGroup(ModItemGroups.FABRICATED_EXCHANGE, PHILOSOPHERS_STONE);
    }

    private static void addToItemGroup(ItemGroup group, Item item) {
        ItemGroupEvents.modifyEntriesEvent(group).register(entries->entries.add(item));
    }

    public static void registerModItems() { 
        FabricatedExchange.LOGGER.info("Registering Mod Items for " + FabricatedExchange.MOD_ID);
        addItemsToItemGroup();
    }   
}

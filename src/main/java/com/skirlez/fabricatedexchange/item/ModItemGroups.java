package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static ItemGroup FABRICATED_EXCHANGE = FabricItemGroup.builder(
            new Identifier(FabricatedExchange.MOD_ID, "fabricated-exchange"))
            .displayName(Text.translatable("itemgroup.fabricated-exchange"))
            .icon(() -> new ItemStack(ModItems.PHILOSOPHERS_STONE)).build();

    public static void registerItemGroups() {
        
    }
}

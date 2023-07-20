package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static BlockEntityType<EnergyCollectorBlockEntity> ENERGY_COLLECTOR = 
        registerBlockEntity("energy_collector_entity",
        FabricBlockEntityTypeBuilder.create(EnergyCollectorBlockEntity::new, 
        ModBlocks.ENERGY_COLLECTOR_MK1, ModBlocks.ENERGY_COLLECTOR_MK2, ModBlocks.ENERGY_COLLECTOR_MK3)
        .build());

    public static BlockEntityType<AntiMatterRelayBlockEntity> ANTIMATTER_RELAY = 
        registerBlockEntity("antimatter_relay_entity",
        FabricBlockEntityTypeBuilder.create(AntiMatterRelayBlockEntity::new, 
        ModBlocks.ANTIMATTER_RELAY_MK1, ModBlocks.ANTIMATTER_RELAY_MK2, ModBlocks.ANTIMATTER_RELAY_MK3)
        .build());


    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, BlockEntityType<T> blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(FabricatedExchange.MOD_ID, name), blockEntityType);
    }

    public static void registerBlockEntities() {

    }
}

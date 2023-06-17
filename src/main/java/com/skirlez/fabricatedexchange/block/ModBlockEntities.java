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
        FabricBlockEntityTypeBuilder.create(EnergyCollectorBlockEntity::new, ModBlocks.ENERGY_COLLECTOR_MK1).build());

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, BlockEntityType<T> blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(FabricatedExchange.MOD_ID, name), blockEntityType);
    }

    public static void registerBlockEntities() {

    }
}

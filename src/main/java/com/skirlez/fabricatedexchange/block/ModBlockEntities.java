package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static BlockEntityType<TransmutationTableBlockEntity> TRANSMUTATION_TABLE;


        
    public static void registerBlockEntities() {
        TRANSMUTATION_TABLE = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            new Identifier(FabricatedExchange.MOD_ID, "transmutation_table"), 
            FabricBlockEntityTypeBuilder.create(TransmutationTableBlockEntity::new, 
            ModBlocks.TRANSMUTATION_TABLE).build(null));

    }
}

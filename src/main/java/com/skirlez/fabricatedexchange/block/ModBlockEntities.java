package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlockEntities {
	public static final BlockEntityType<EnergyCollectorBlockEntity> ENERGY_COLLECTOR = 
		registerBlockEntity("energy_collector_entity",
		FabricBlockEntityTypeBuilder.create(EnergyCollectorBlockEntity::new, 
		ModBlocks.ENERGY_COLLECTOR_MK1, ModBlocks.ENERGY_COLLECTOR_MK2, ModBlocks.ENERGY_COLLECTOR_MK3)
		.build());

	public static final BlockEntityType<AntiMatterRelayBlockEntity> ANTIMATTER_RELAY = 
		registerBlockEntity("antimatter_relay_entity",
		FabricBlockEntityTypeBuilder.create(AntiMatterRelayBlockEntity::new, 
		ModBlocks.ANTIMATTER_RELAY_MK1, ModBlocks.ANTIMATTER_RELAY_MK2, ModBlocks.ANTIMATTER_RELAY_MK3)
		.build());

	public static final BlockEntityType<AlchemicalChestBlockEntity> ALCHEMICAL_CHEST = 
		registerBlockEntity("alchemical_chest_entity",
		FabricBlockEntityTypeBuilder.create(AlchemicalChestBlockEntity::new, 
		ModBlocks.ALCHEMICAL_CHEST)
		.build());

	public static final BlockEntityType<EnergyCondenserBlockEntity> ENERGY_CONDENSER = 
		registerBlockEntity("energy_condenser_entity",
		FabricBlockEntityTypeBuilder.create(EnergyCondenserBlockEntity::new, 
		ModBlocks.ENERGY_CONDENSER_MK1,ModBlocks.ENERGY_CONDENSER_MK2)
		.build());



	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(String name, BlockEntityType<T> blockEntityType) {
		return Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(FabricatedExchange.MOD_ID, name), blockEntityType);
	}

	public static void registerBlockEntities() {

	}
}

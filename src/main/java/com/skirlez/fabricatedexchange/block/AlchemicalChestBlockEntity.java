package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.screen.AlchemicalChestScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;

public class AlchemicalChestBlockEntity extends BaseChestBlockEntity {
	public AlchemicalChestBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.ALCHEMICAL_CHEST, pos, state);
	}

	public AlchemicalChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
	}

	@Override
	public int size() {
		return 104;
	}

	@Override
	public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new AlchemicalChestScreenHandler(syncId, playerInventory, this);
	}
}
package com.skirlez.fabricatedexchange.block;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class EnergyCondenser extends ChestBlock {
	private final int level;
	public EnergyCondenser(Settings settings, int level) {
		super(settings, new Supplier<BlockEntityType<? extends ChestBlockEntity>>() {
			@Override
			public BlockEntityType<? extends ChestBlockEntity> get() {
				return ModBlockEntities.ENERGY_CONDENSER;
			}
		});
		this.level = level;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new EnergyCondenserBlockEntity(pos, state);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		Direction direction = context.getHorizontalPlayerFacing().getOpposite();
		FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
		return this.getDefaultState().with(FACING, direction).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return (world.isClient && type == ModBlockEntities.ENERGY_CONDENSER) 
			? (world2, pos, state2, blockEntity) -> ((EnergyCondenserBlockEntity)blockEntity).clientTick(world2, pos, state2) 
			: (world2, pos, state2, blockEntity) -> ((EnergyCondenserBlockEntity)blockEntity).serverTick(world2, pos, state2);
	}


	public int getLevel() {
		return level;
	}
}
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

public class AlchemicalChest extends ChestBlock {
    public AlchemicalChest(Settings settings) {
        super(settings, new Supplier<BlockEntityType<? extends ChestBlockEntity>>() {
            @Override
            public BlockEntityType<? extends ChestBlockEntity> get() {
                return ModBlockEntities.ALCHEMICAL_CHEST;
            }
        });
    }


    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AlchemicalChestBlockEntity(pos, state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction direction = context.getPlayerFacing().getOpposite();
        FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
        return this.getDefaultState().with(FACING, direction).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world.isClient && type == ModBlockEntities.ALCHEMICAL_CHEST) 
            ? (world2, pos, state1, blockEntity) -> ((AlchemicalChestBlockEntity)blockEntity).progressAnimation() 
            : null;
    }
}
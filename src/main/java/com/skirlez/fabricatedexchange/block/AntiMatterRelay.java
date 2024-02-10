package com.skirlez.fabricatedexchange.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AntiMatterRelay extends BlockWithEntityAndRotation {
	private final int level;

	public AntiMatterRelay(Settings settings, int level) {
		super(settings);
		this.level = level;
	}
	@Override   
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, 
		BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			NamedScreenHandlerFactory screenHandlerFactory = ((AntiMatterRelayBlockEntity)world.getBlockEntity(pos));
			if (screenHandlerFactory != null) 
				player.openHandledScreen(screenHandlerFactory);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof AntiMatterRelayBlockEntity antimatterRelay) {
				ItemScatterer.spawn(world, pos, antimatterRelay);
				world.updateComparators(pos, this);
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}


	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new AntiMatterRelayBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return type != ModBlockEntities.ANTIMATTER_RELAY ? null : 
			(world.isClient()) 
			? (world2, pos, state2, blockEntity) 
				-> AntiMatterRelayBlockEntity.clientTick(world2, pos, state2, (AntiMatterRelayBlockEntity)blockEntity)
			: (world2, pos, state2, blockEntity) 
				-> AntiMatterRelayBlockEntity.serverTick(world2, pos, state2, (AntiMatterRelayBlockEntity)blockEntity);

	}

	public int getLevel() {
		return level;
	}
}
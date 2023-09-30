package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandlerFactory;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TransmutationTable extends Block {
	public TransmutationTable(Settings settings) {
		super(settings);
	}

	private static final VoxelShape SHAPE = VoxelShapes.cuboid(0f, 0f, 0f, 1f, 0.25f, 1.0f);

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override   
	public BlockRenderType getRenderType(BlockState state) {	
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, 
		BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient) {
			TransmutationTableScreenHandlerFactory screenHandlerFactory = new TransmutationTableScreenHandlerFactory();
			player.openHandledScreen(screenHandlerFactory);
		}

		return ActionResult.SUCCESS;
	}
}
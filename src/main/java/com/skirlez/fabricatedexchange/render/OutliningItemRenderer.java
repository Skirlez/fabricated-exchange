package com.skirlez.fabricatedexchange.render;

import com.skirlez.fabricatedexchange.item.OutliningItem;
import com.skirlez.fabricatedexchange.mixin.client.ModWorldRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public final class OutliningItemRenderer {
	private OutliningItemRenderer() {};

	public static void register() {
		WorldRenderEvents.BLOCK_OUTLINE.register(((WorldRenderContext worldRenderContext, WorldRenderContext.BlockOutlineContext blockOutlineContext) -> {
			render(worldRenderContext, blockOutlineContext);
			return true;
		}));
	}

	private static void render(WorldRenderContext worldRenderContext, WorldRenderContext.BlockOutlineContext blockOutlineContext) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientWorld world = client.world;

		ClientPlayerEntity p = client.player;
		ItemStack stack = p.getStackInHand(Hand.MAIN_HAND);
		if (!(stack.getItem() instanceof OutliningItem)) {
			stack = p.getStackInHand(Hand.OFF_HAND);
			if (!(stack.getItem() instanceof OutliningItem))
				return;
		}
		OutliningItem item = (OutliningItem)stack.getItem();

		VertexConsumer vertexConsumer = worldRenderContext.consumers().getBuffer(RenderLayer.LINES);
		List<BlockPos> positions = item.getPositionsToOutline(p, stack, blockOutlineContext.blockPos());
		for (BlockPos pos : positions) {
			BlockState newBlockState = world.getBlockState(pos);
			ModWorldRenderer.invokeDrawCuboidShapeOutline(worldRenderContext.matrixStack(), vertexConsumer,
				newBlockState.getOutlineShape(world, pos, ShapeContext.of(blockOutlineContext.entity())),
				(double)pos.getX() - blockOutlineContext.cameraX(),
				(double)pos.getY() - blockOutlineContext.cameraY(),
				(double)pos.getZ() - blockOutlineContext.cameraZ(),
				0.0f, 0.0f, 0.0f, 0.4f);
		}

	}

}

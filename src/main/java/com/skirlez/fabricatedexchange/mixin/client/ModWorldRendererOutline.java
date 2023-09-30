package com.skirlez.fabricatedexchange.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.skirlez.fabricatedexchange.item.OutliningItem;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

@Mixin(WorldRenderer.class)
public abstract class ModWorldRendererOutline {

	@Shadow 
	private ClientWorld world;

	@Shadow
	private MinecraftClient client;
	
	@Shadow
	private static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {
		
	}

	@Inject(method = "drawBlockOutline", at = @At("HEAD"))
	private void drawEvenMoreDrawBlockOutlines(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo cir) {
		ClientPlayerEntity p = client.player;
		ItemStack stack = p.getStackInHand(Hand.MAIN_HAND);
		if (!(stack.getItem() instanceof OutliningItem)) {
			stack = p.getStackInHand(Hand.OFF_HAND);
			if (!(stack.getItem() instanceof OutliningItem))
				return;
		}
		OutliningItem item = (OutliningItem)stack.getItem();

		BlockHitResult hit = (BlockHitResult)client.crosshairTarget;
		BlockPos originalPos = new BlockPos(pos);
		if (hit.getType().equals(HitResult.Type.BLOCK)
				&& item.outlineEntryCondition(state)) {
			List<BlockPos> positions = item.getPositionsToOutline(p, stack, originalPos);
			for (int i = 0; i < positions.size(); i++) {
				BlockPos newPos = positions.get(i);
				BlockState newBlockState = world.getBlockState(newPos);
				drawCuboidShapeOutline(matrices, vertexConsumer, 
					newBlockState.getOutlineShape(this.world, newPos, ShapeContext.of(entity)), 
					(double)newPos.getX() - cameraX, (double)newPos.getY() - cameraY, 
					(double)newPos.getZ() - cameraZ, 0.0f, 0.0f, 0.0f, 0.4f);   
			}

		}
	}
}
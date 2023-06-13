package com.skirlez.fabricatedexchange.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.ModItems;

import net.minecraft.block.Block;
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

    @Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
    private void injectDrawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state, CallbackInfo cir) {
        ClientPlayerEntity p = client.player;
        ItemStack stack = p.getStackInHand(Hand.MAIN_HAND);
        if (!stack.getItem().equals(ModItems.PHILOSOPHERS_STONE)) {
            stack = p.getStackInHand(Hand.OFF_HAND);
            if (!stack.getItem().equals(ModItems.PHILOSOPHERS_STONE))
                return;
        }
        int charge = stack.getOrCreateNbt().getInt("Charge");
        BlockHitResult hit = (BlockHitResult)client.crosshairTarget;
        Block block = world.getBlockState(pos).getBlock();
        if (charge != 0 
                && hit.getType().equals(HitResult.Type.BLOCK)
                && FabricatedExchange.blockTransmutationMap.containsKey(block)) {
            int xOff = -charge, yOff = -charge, zOff = -charge;
            switch (hit.getSide()) {
                case DOWN:
                    yOff += charge;
                    break;
                case UP:
                    yOff -= charge;
                    break;
                case NORTH:
                    zOff += charge;
                    break;
                case SOUTH:
                    zOff -= charge;
                    break;
                case WEST:
                    xOff += charge;
                    break;
                case EAST:
                    xOff -= charge;
                    break;
                default:
                    FabricatedExchange.LOGGER.error("Unknown block side in outline rendering. Side: " + hit.getSide().toString());
                    break;
            };
            pos = pos.add(xOff, yOff, zOff);
            int len = charge * 2 + 1;
            for (int x = 0; x < len; x++) {
                for (int y = 0; y < len; y++) {
                    for (int z = 0; z < len; z++) {
                        BlockPos newPos = pos.add(x, y, z);
                        Block newBlock = world.getBlockState(newPos).getBlock();
                        if (!newBlock.equals(block))
                            continue;
                        drawCuboidShapeOutline(matrices, vertexConsumer, 
                                state.getOutlineShape(this.world, newPos, ShapeContext.of(entity)), 
                                (double)newPos.getX() - cameraX, (double)newPos.getY() - cameraY, 
                                (double)newPos.getZ() - cameraZ, 0.0f, 0.0f, 0.0f, 0.4f);
                    }
                }
            }
            // TODO: check cancelling screws over any other mod which might inject to this method
            // (we cancel to not redundantly draw the outline where the vanilla target block is)
            cir.cancel();
            
            
        }
        
    }
    

}
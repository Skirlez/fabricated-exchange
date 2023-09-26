package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.skirlez.fabricatedexchange.item.PreMiningItem;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerPreMine {
    
    @Shadow private ServerPlayerEntity player;
    @Shadow private ServerWorld world;

    @Inject(method = "tryBreakBlock", at =  @At(value = 
        "INVOKE", 
    target = "Lnet/minecraft/block/Block;onBreak(" + 
        "Lnet/minecraft/world/World;" +
        "Lnet/minecraft/util/math/BlockPos;" + 
        "Lnet/minecraft/block/BlockState;" +
        "Lnet/minecraft/entity/player/PlayerEntity;" +
        ")V"))
    private void tryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (player.isCreative())
            return;
        ItemStack stack = player.getMainHandStack();
        if (stack.getItem() instanceof PreMiningItem item) {
            item.preMine(stack, world, world.getBlockState(pos), pos, player);

        }
    }
}

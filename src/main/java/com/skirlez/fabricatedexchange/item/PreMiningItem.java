package com.skirlez.fabricatedexchange.item;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PreMiningItem {
    void preMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner);
}

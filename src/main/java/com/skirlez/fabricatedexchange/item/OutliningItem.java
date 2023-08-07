package com.skirlez.fabricatedexchange.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

// Items that should highlight multiple blocks implement this interface.
public interface OutliningItem {
    boolean outlineEntryCondition(BlockState block);

    List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center);
}

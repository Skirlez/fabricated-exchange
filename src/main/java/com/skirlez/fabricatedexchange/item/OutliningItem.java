package com.skirlez.fabricatedexchange.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/** Items that should highlight multiple blocks implement this interface.
 * The outlines themselves are drawn by {@link com.skirlez.fabricatedexchange.mixin.client.ModWorldRendererOutline}. */
public interface OutliningItem {
	boolean outlineEntryCondition(BlockState state);

	List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center);
}

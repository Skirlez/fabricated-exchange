package com.skirlez.fabricatedexchange.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/** Items that should highlight multiple blocks implement this interface.
 * The outlines themselves are drawn by {@link com.skirlez.fabricatedexchange.mixin.client.ModWorldRendererOutline}. */
public interface OutliningItem {
	
	/** Implementing classes can use this to determine whether or not to outline a block. */
	boolean outlineEntryCondition(BlockState state);

	/** The list of positions returned by implementing classes using this method will be outlined. */
	List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center);
}

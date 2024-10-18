package com.skirlez.fabricatedexchange.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/** Items that should highlight multiple blocks implement this interface.
 * The outlines themselves are drawn by {@link com.skirlez.fabricatedexchange.render.OutliningItemRenderer}. */
public interface OutliningItem {
	List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos selectedBlockPos);
}

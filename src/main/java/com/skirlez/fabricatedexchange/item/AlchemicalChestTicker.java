package com.skirlez.fabricatedexchange.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/** Items that implement this interface will have their defined tick method called when inside an alchemical chest.
 * If there are two ItemStacks or more of this item, only the first one will have its method called. */
public interface AlchemicalChestTicker {
	void alchemicalChestTick(World world, BlockPos pos, List<ItemStack> inventory);
}

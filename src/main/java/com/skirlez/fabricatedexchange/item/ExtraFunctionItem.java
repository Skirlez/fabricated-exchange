package com.skirlez.fabricatedexchange.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface ExtraFunctionItem {
	void doExtraFunction(World world, PlayerEntity player, ItemStack stack);
}

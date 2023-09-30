package com.skirlez.fabricatedexchange.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class BlocklessCraftingScreenHandler extends CraftingScreenHandler {

	public BlocklessCraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
		super(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.getWorld(), playerInventory.player.getBlockPos()));
	}
	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}
}

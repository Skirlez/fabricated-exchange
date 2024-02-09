package com.skirlez.fabricatedexchange.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

// This slot will not consider input from clicking. It is used as the target slot for the energy collectors and condensers
public class DisplaySlot extends Slot {
	public DisplaySlot(Inventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}
	@Override
	public boolean canInsert(ItemStack stack) {
		return false;
	}
	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		return false;
	}
}

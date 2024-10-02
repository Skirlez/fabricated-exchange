package com.skirlez.fabricatedexchange.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.function.Function;

// This slot will not consider input from clicking. It is used as the target slot for the energy collectors and condensers
public class DisplaySlot extends Slot {

	private Function<ItemStack, ItemStack> process;
	public DisplaySlot(Inventory inventory, int index, int x, int y) {
		this(inventory, index, x, y, Function.identity());
	}
	/** You may provide a process function that gets run on any stack that's put in the slot. */
	public DisplaySlot(Inventory inventory, int index, int x, int y, Function<ItemStack, ItemStack> process) {
		super(inventory, index, x, y);
		this.process = process;
	}
	@Override
	public boolean canInsert(ItemStack stack) {
		return false;
	}
	@Override
	public boolean canTakeItems(PlayerEntity playerEntity) {
		return false;
	}

	@Override
	public void setStack(ItemStack stack) {
		stack = process.apply(stack);
		super.setStack(stack);
	}
}

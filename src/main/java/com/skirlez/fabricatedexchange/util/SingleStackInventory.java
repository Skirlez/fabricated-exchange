package com.skirlez.fabricatedexchange.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;



public class SingleStackInventory implements Inventory {
	private static final String STUPID_ERROR = "Index out of range, there is only one item.";
	private ItemStack stack;
	
	public SingleStackInventory() {
		this.stack = Items.AIR.getDefaultStack();
	}
	public SingleStackInventory(ItemStack stack) {
		this.stack = stack;
	}
	public ItemStack getStack() {
		return stack;
	}
	@Override
	public ItemStack getStack(int index) {
		if (index != 0)
			throw new UnsupportedOperationException(STUPID_ERROR);
		return stack;
	}

	@Override
	public ItemStack removeStack(int index, int count) {
		if (index != 0)
			throw new UnsupportedOperationException(STUPID_ERROR);
		stack.decrement(count);
		return stack;
	}
	@Override
	public void setStack(int index, ItemStack stack) {
		if (index != 0)
			throw new UnsupportedOperationException(STUPID_ERROR);
		this.stack = stack;
	}

	@Override
	public void markDirty() {
		
	}
	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}
	
	@Override
	public void clear() {
		stack = Items.AIR.getDefaultStack();
	}
	@Override
	public int size() {
		return 1;
	}
	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}
	@Override
	public ItemStack removeStack(int index) {
		ItemStack store = stack;
		stack = Items.AIR.getDefaultStack();
		return store;
	}



}

package com.skirlez.fabricatedexchange.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;


// I really do not understand why the original is an interface. Also, this class looks really dumb.
public class SingleStackInventoryImpl implements SingleStackInventory {
	private static final String STUPID_ERROR = "Index out of range, there is only one item.";
	private ItemStack stack;
	
	public SingleStackInventoryImpl() {
		this.stack = Items.AIR.getDefaultStack();
	}
	public SingleStackInventoryImpl(ItemStack stack) {
		this.stack = stack;
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



}

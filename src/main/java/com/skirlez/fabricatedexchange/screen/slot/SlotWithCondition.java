package com.skirlez.fabricatedexchange.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class SlotWithCondition extends ConsiderateSlot {
	private StackCondition condition;
	public SlotWithCondition(Inventory inventory, int index, int x, int y, StackCondition condition) {
		super(inventory, index, x, y);
		this.condition = condition;
	}
	
	@Override
	public boolean canInsert(ItemStack stack) {
		return super.canInsert(stack) && this.condition.passes(stack);
	}

}

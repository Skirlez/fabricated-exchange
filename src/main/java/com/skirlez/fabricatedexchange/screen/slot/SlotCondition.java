package com.skirlez.fabricatedexchange.screen.slot;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.minecraft.item.ItemStack;

public interface SlotCondition {
	boolean condition(ItemStack stack);
	public static SlotCondition alwaysTrue = (stack) -> true;
	public static SlotCondition isFuel = (stack) -> FabricatedExchange.fuelProgressionMap.containsKey(stack.getItem());
}


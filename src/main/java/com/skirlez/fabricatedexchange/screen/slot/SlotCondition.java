package com.skirlez.fabricatedexchange.screen.slot;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.minecraft.item.ItemStack;

public interface SlotCondition {
	boolean passes(ItemStack stack);
	public static SlotCondition always = (stack) -> true;
	public static SlotCondition never = (stack) -> false;
	public static SlotCondition isFuel = (stack) -> FabricatedExchange.fuelProgressionMap.containsKey(stack.getItem());
}


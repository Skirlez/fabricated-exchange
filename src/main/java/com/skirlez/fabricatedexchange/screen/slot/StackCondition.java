package com.skirlez.fabricatedexchange.screen.slot;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.minecraft.item.ItemStack;

public interface StackCondition {
	boolean passes(ItemStack stack);
	public static StackCondition always = (stack) -> true;
	public static StackCondition never = (stack) -> false;
	public static StackCondition isFuel = (stack) -> FabricatedExchange.fuelProgressionMap.containsKey(stack.getItem());
	public static StackCondition isBattery = (stack) -> false; // TODO
}


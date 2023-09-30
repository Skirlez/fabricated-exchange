package com.skirlez.fabricatedexchange.item;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public interface ExtraFunctionItem {
	void doExtraFunction(ItemStack stack, ServerPlayerEntity player);

	default void doExtraFunctionClient(ItemStack stack, ClientPlayerEntity player) {

	}
}

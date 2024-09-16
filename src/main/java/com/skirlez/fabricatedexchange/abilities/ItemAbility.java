package com.skirlez.fabricatedexchange.abilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public abstract class ItemAbility {
	public abstract void tick(ItemStack stack, PlayerEntity player);
	public abstract void onRemove(PlayerEntity player);
}

package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.abilities.ItemAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface AbilityGrantingItem {
	public boolean shouldGrantAbility(PlayerEntity player, ItemStack stack);
	public ItemAbility getAbility();
}

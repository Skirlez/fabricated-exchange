package com.skirlez.fabricatedexchange.item.BaseFunction;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;

public class FlyingAbilityItem extends Item {
	// Use a WeakHashMap to avoid memory leaks by not preventing player entities from being garbage-collected.
	private static final Map<PlayerEntity, Boolean> playerHadItemMap = new WeakHashMap<>();

	public FlyingAbilityItem(Settings settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);

		if (entity instanceof ServerPlayerEntity && !world.isClient) {
			ServerPlayerEntity player = (ServerPlayerEntity) entity;

			// Check if the player's inventory contains this specific item.
			// This replaces the previous hasItemNow check with a more thorough search.
			boolean hasItemNow = hasItemInInventory(player, this);

			// Retrieve the previously stored state for this player indicating if they had the item.
			Boolean hadItemBefore = playerHadItemMap.getOrDefault(player, false);

			if (hasItemNow && !player.getAbilities().allowFlying) {
				// Grant flying ability
				player.getAbilities().allowFlying = true;
				player.sendAbilitiesUpdate();
			} 
			else if (!hasItemNow && hadItemBefore && player.getAbilities().allowFlying && !player.isCreative() && !player.isSpectator()) {
				// Revoke flying ability
				player.getAbilities().allowFlying = false;
				player.getAbilities().flying = false;
				player.sendAbilitiesUpdate();
			}

			if (player.getAbilities().flying) {
				stack.getOrCreateNbt().putInt("CustomModelData", 1);
			}
			else {
				stack.getOrCreateNbt().putInt("CustomModelData", 0);
			}

			// Update the stored state for this player.
			playerHadItemMap.put(player, hasItemNow);
		}
	}

	private boolean hasItemInInventory(ServerPlayerEntity player, Item item) {
		return player.getInventory().main.stream().anyMatch(stack -> stack.getItem().equals(item));
	}
}

package com.skirlez.fabricatedexchange.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class FlyingAbilityItem extends Item {
	// Use a WeakHashMap to avoid memory leaks by not preventing player entities from being garbage-collected.
	private static final Map<PlayerEntity, Boolean> playerHadItemMap = new WeakHashMap<PlayerEntity, Boolean>();

	public FlyingAbilityItem(Settings settings) {
		super(settings);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);

		if (entity instanceof PlayerEntity player) {
			determinePlayerFlightStatus(player, stack);
		}
	}
	
	private void determinePlayerFlightStatus(PlayerEntity player, ItemStack stack) {
		// Check if the player's inventory contains this specific item.
		// This replaces the previous hasItemNow check with a more thorough search.
		boolean hasItemNow = hasItemInInventory(player, this) && flyCondition(player, stack);
		// Retrieve the previously stored state for this player indicating if they had the item.
		boolean hadItemBefore = playerHadItemMap.getOrDefault(player, false);
		if (hasItemNow && !player.getAbilities().allowFlying) {
			enablePlayerFlying(player, stack);
		} 
		else if (!hasItemNow && hadItemBefore && player.getAbilities().allowFlying) {
			disablePlayerFlying(player, stack);
		}

		// Update the stored state for this player.
		playerHadItemMap.put(player, hasItemNow);
	}
	
	
	public void onDropped(PlayerEntity player, ItemStack stack) {
		determinePlayerFlightStatus(player, stack);
	}
	

	private boolean hasItemInInventory(PlayerEntity player, Item item) {
		return player.getInventory().main.stream().anyMatch(stack -> stack.getItem().equals(item));
	}
	
	protected boolean flyCondition(PlayerEntity player, ItemStack stack) {
		return true;
	}
	protected void onFlightEnable(PlayerEntity player, ItemStack stack) {
	}
	
	protected void onFlightDisable(PlayerEntity player, ItemStack stack) {	
	}
	
	public static void enablePlayerFlying(PlayerEntity player, ItemStack stack) {
		player.getAbilities().allowFlying = true;
		((FlyingAbilityItem)stack.getItem()).onFlightEnable(player, stack);
		player.sendAbilitiesUpdate();
	}
	
	public static void disablePlayerFlying(PlayerEntity player, ItemStack stack) {
		((FlyingAbilityItem)stack.getItem()).onFlightDisable(player, stack);
		if (!player.isCreative() && !player.isSpectator()) {
			player.getAbilities().allowFlying = false;
			player.getAbilities().flying = false;
			player.sendAbilitiesUpdate();
		}
	}
}

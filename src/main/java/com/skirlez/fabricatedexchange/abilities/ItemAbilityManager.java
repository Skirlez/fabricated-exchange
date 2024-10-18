package com.skirlez.fabricatedexchange.abilities;

import com.skirlez.fabricatedexchange.item.AbilityGrantingItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;


public class ItemAbilityManager {
	private ItemAbilityManager() { }

	public static void registerServer() {
		final Map<PlayerEntity, List<ItemAbility>> abilityMap = new WeakHashMap<PlayerEntity, List<ItemAbility>>();
		ServerTickEvents.END_SERVER_TICK.register((server) -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				handlePlayerAbilities(player, abilityMap);
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			// I'm fairly sure this is safe to do...
			// By doing this I'm trying to avoid any shenanigans with the player
			// leaving and somehow throwing their item at the same time, so onRemove never gets called.
			List<ItemAbility> abilities = abilityMap.getOrDefault(handler.player, new ArrayList<ItemAbility>());
			for (ItemAbility ability : abilities) {
				ability.onRemove(handler.player);
			}
			abilityMap.remove(handler.player);
		});
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		final Map<PlayerEntity, List<ItemAbility>> abilityMap = new HashMap<PlayerEntity, List<ItemAbility>>();
		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			// I'm not sure why, but unless we treat client.player like this, Java will try to load
			// ClientPlayerEntity when the server calls registerServer() (causing a crash)
			PlayerEntity player = (PlayerEntity) (Object) client.player;
			if (player != null) {
				handlePlayerAbilities(player, abilityMap);
			}
		});
	}



	private static void handlePlayerAbilities(PlayerEntity player, Map<PlayerEntity, List<ItemAbility>> abilityMap) {
		List<ItemAbility> abilities = new ArrayList<ItemAbility>();
		List<ItemStack> items = new ArrayList<ItemStack>();

		PlayerInventory inventory = player.getInventory();

		for (int i = 0; i < inventory.size(); i++) {
			if (!(inventory.getStack(i).getItem() instanceof AbilityGrantingItem item))
				continue;
			if (!item.shouldGrantAbility(player, inventory.getStack(i)))
				continue;
			if (!abilities.contains(item.getAbility())) {
				abilities.add(item.getAbility());
				items.add(inventory.getStack(i));
			}
		}

		List<ItemAbility> previousAbilities = abilityMap.getOrDefault(player, new ArrayList<ItemAbility>());

		// Abilities we had before, but don't anymore
		List<ItemAbility> removedAbilities = new ArrayList<ItemAbility>(previousAbilities);
		removedAbilities.removeAll(abilities);
		for (ItemAbility ability : removedAbilities) {
			ability.onRemove(player);
		}
		for (int i = 0; i < abilities.size(); i++) {
			abilities.get(i).tick(items.get(i), player);
		}

		if (!previousAbilities.equals(abilities))
			abilityMap.put(player, new ArrayList<ItemAbility>(abilities));
	}

}

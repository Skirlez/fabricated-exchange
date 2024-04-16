package com.skirlez.fabricatedexchange.screen.slot.transmutation;

import java.util.Iterator;
import java.util.List;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.NbtItem;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

public class ConsumeSlot extends Slot {
	// This slot destroys any item put inside and adds its EMC to it to a player.
	// if the item doesn't have EMC, it rejects it.
	private PlayerEntity player;
	private TransmutationTableScreenHandler screenHandler;
	public ConsumeSlot(Inventory inventory, int index, int x, int y, PlayerEntity player, TransmutationTableScreenHandler screenHandler) {
		super(inventory, index, x, y);
		this.player = player;
		this.screenHandler = screenHandler;
	}

	@Override
	public ItemStack insertStack(ItemStack stack, int count) {
		Item item = stack.getItem();
		ItemStack originalStack = stack;
		
		stack = stack.copy();
		stack.setCount(count);
		SuperNumber emc = EmcData.getItemStackEmc(stack);
		if (emc.equalsZero() && !item.equals(ModItems.TOME_OF_KNOWLEDGE))
			return originalStack;
		if (!player.getWorld().isClient()) {
			EmcData.addEmc((ServerPlayerEntity)player, emc);
			PlayerState playerState = ServerState.getPlayerState(player);
			String idName = Registry.ITEM.getId(item).toString();
			if (ModDataFiles.NBT_ITEMS.hasItem(idName)) {
				List<String> allowedKeys = ModDataFiles.NBT_ITEMS.getAllowedKeys(idName);
				NbtCompound nbt = stack.getNbt();
				if (nbt == null)
					nbt = new NbtCompound();
				if (!nbt.isEmpty()) {
					Iterator<String> keyIterator = nbt.getKeys().iterator();
					while (keyIterator.hasNext()) {
						String key = keyIterator.next();
						if (!allowedKeys.contains(key))
							keyIterator.remove();
					}
				}
				boolean match = false;
				NbtItem nbtItem = new NbtItem(item, nbt);
				for (NbtItem currentNbtItem : playerState.specialKnowledge) {
					if (nbtItem.equalTo(currentNbtItem)) {
						match = true;
						break;
					}
				}
				if (match == false) {
					playerState.specialKnowledge.add(nbtItem);
					screenHandler.addKnowledge(nbtItem);
				}
			}
			else {
				if (item.equals(ModItems.TOME_OF_KNOWLEDGE)) {	
					Registry.ITEM.forEach(
					currentItem -> {
						String currentId = Registry.ITEM.getId(currentItem).toString();
						if (ModDataFiles.NBT_ITEMS.hasItem(currentId))
							return;
						SuperNumber currentEmc = EmcData.getItemEmc(currentItem);
						if (!currentEmc.equalsZero() && !playerState.knowledge.contains(currentItem)) {
							playerState.knowledge.add(currentItem);
							screenHandler.addKnowledge(new NbtItem(currentItem));
						}
					});
					
				}
				else if (!playerState.knowledge.contains(item)) {
					playerState.knowledge.add(item);
					screenHandler.addKnowledge(new NbtItem(item));
				}
			}
			playerState.markDirty();
			screenHandler.refreshOffering();	
		}

		// diff is the amount of the item the player had minus the amount they put in. if it's zero we give empty, otherwise we give what's left back.
		int diff = originalStack.getCount() - count;
		if (diff == 0) 
			return ItemStack.EMPTY; 
		stack.setCount(diff);
		return stack;
	}




	@Override
	public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
		return super.takeStackRange(min, max, player);
	}

	// these two methods make sure that you can't actually insert anything into the slot by any means
	// well, inventory *is* public, so you could, but i would not like you
	@Override
	public void setStack(ItemStack stack) {
		
	}

	public boolean canInsert(ItemStack stack) {
		return false;
	}


	
}

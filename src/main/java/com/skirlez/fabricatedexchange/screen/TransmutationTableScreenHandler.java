package com.skirlez.fabricatedexchange.screen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.NbtItem;
import com.skirlez.fabricatedexchange.packets.ModServerToClientPackets;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.ConsumeSlot;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.ForgetSlot;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.MidSlot;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.TransmutationSlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class TransmutationTableScreenHandler extends ScreenHandler {
	private final Inventory inventory;
	private final Inventory transmutationInventory;
	private final PlayerEntity player;
	private final ConsumeSlot emcSlot;
	private String searchText = "";
	private int offeringPageNum = 0;
	private int lastOfferingPage = 0;
	
	private int playerSlotsStart;
	
	private List<NbtItem> orderedKnowledge = new ArrayList<NbtItem>();
	private final DefaultedList<TransmutationSlot> transmutationSlots = DefaultedList.of();

	public static TransmutationTableScreenHandler clientConstructor(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		TransmutationTableScreenHandler ret = new TransmutationTableScreenHandler(syncId, playerInventory);
		ret.lastOfferingPage = buf.readInt();
		return ret;
		
	}
	
	
	public TransmutationTableScreenHandler(int syncId, PlayerInventory playerInventory) {
		super(ModScreenHandlers.TRANSMUTATION_TABLE, syncId);
		this.inventory = new SimpleInventory(3);
		this.transmutationInventory = new SimpleInventory(16);
		this.player = playerInventory.player;
		
		inventory.onOpen(playerInventory.player);
		
		addSlot(new MidSlot(inventory, 0, 159, 49, this, player.getWorld().isClient()));
		addSlot(new ForgetSlot(inventory, 1, 91, 97, player, this));
		emcSlot = new ConsumeSlot(inventory, 2, 109, 97, player, this);
		addSlot(emcSlot);
		
		// use trigonometry to create the transmutation slots

		// outer ring
		double angle = 270.0;
		for (int i = 0; i < 12; i++) {
			addTransmutationSlot(new TransmutationSlot(transmutationInventory, i, angle, player, this));
			angle += 360.0 / 12.0;
		}

		// inner ring
		angle = 270.0;
		for (int i = 0; i < 4; i++) {
			addTransmutationSlot(new TransmutationSlot(transmutationInventory, i + 12, angle, player, this));
			angle += 360.0 / 4.0;
		}
		
		playerSlotsStart = slots.size();

		GeneralUtil.addPlayerInventory(this, playerInventory, 37, 117);
		GeneralUtil.addPlayerHotbar(this, playerInventory, 37, 175);

		if (!player.getWorld().isClient()) {
			PlayerState playerState = ServerState.getPlayerState(player);
			for (Item item : playerState.knowledge)
				addKnowledge(new NbtItem(item));
			for (NbtItem item : playerState.specialKnowledge)
				addKnowledge(item);
			refreshOffering();
		}
	}

	public void addKnowledge(NbtItem item) {
		int index = Collections.binarySearch(orderedKnowledge, item, 
			(item1, item2) -> 
			{
				int comparison = EmcData.getItemEmc(item2).compareTo(EmcData.getItemEmc(item1));
				if (comparison != 0)
					return comparison;
				return Registries.ITEM.getId(item1.asItem()).compareTo(Registries.ITEM.getId(item2.asItem()));
			});

		if (index < 0)
			index = -index - 1;

		orderedKnowledge.add(index, item);
	}

	public void removeKnowledge(NbtItem item) {
		int index = Collections.binarySearch(orderedKnowledge, item, 
			(item1, item2) -> 
				EmcData.getItemEmc(item2)
				.compareTo(
				EmcData.getItemEmc(item1)));

		if (index < 0)
			return;

		SuperNumber targetEmc = EmcData.getItemEmc(item);

		for (int i = index - 1; i >= 0; i--) {
			NbtItem currentItem = orderedKnowledge.get(i);
			if (currentItem.equalTo(item)) {
				orderedKnowledge.remove(i);
				return;
			}
			if (!EmcData.getItemEmc(currentItem).equals(targetEmc))
				break;
		}

		if (orderedKnowledge.get(index).equalTo(item)) {
			orderedKnowledge.remove(index);
			return;
		}

		for (int i = index + 1; i < orderedKnowledge.size(); i++) {
			NbtItem currentItem = orderedKnowledge.get(i);
			if (currentItem.equalTo(item)) {
				orderedKnowledge.remove(i);
				return;
			}
			if (!EmcData.getItemEmc(currentItem).equals(targetEmc))
				break;
		}


	}
	public void clearKnowledge() {
		orderedKnowledge = new ArrayList<NbtItem>();
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
		offeringPageNum = 0;
	}
	public void changeOfferingPage(int value) {
		offeringPageNum = value;
		refreshOffering();
	}

	public int getLastPageNum() {
		return lastOfferingPage;
	}

	public void setLastPageNum(int lastOfferingPage) {
		this.lastOfferingPage = lastOfferingPage;
	}
	
	public void refreshOffering() {
		SuperNumber emc = ServerState.getPlayerState(player).emc;
		SuperNumber midItemEmc = EmcData.getItemStackEmc(slots.get(0).getStack());
		if (!midItemEmc.equalsZero())
			emc = SuperNumber.min(emc, midItemEmc);
		
		List<ItemStack> outerRing = new LinkedList<ItemStack>();
		List<ItemStack> innerRing = new LinkedList<ItemStack>();
		boolean isSearching = !searchText.isEmpty();

		int newKnowledgeSize = 0;
		for (int i = 0; i < orderedKnowledge.size(); i++) {
			NbtItem item = orderedKnowledge.get(i);
			SuperNumber itemEmc = EmcData.getItemEmc(item);
			// emc filter - items who's emc value is greater than the players' emc shouldn't be displayed
			// (or if the item has 0 EMC which can happen if you learn it and then set the emc to 0)
			if (emc.compareTo(itemEmc) == -1 || itemEmc.equalsZero()) 
				continue;			

			// search filter - items who don't have the search text as a substring shouldn't be displayed.
			// TODO: this does not work for languages other than english.
			String name = item.asItem().getName().getString();
			if (isSearching && !name.toLowerCase().contains(searchText.toLowerCase())) 
				continue; 
			
			// fuel items go in the inner ring
			if (FabricatedExchange.fuelProgressionMap.containsKey(item.asItem())) {
				if (innerRing.size() < 4)
					innerRing.add(item.asItemStack());
			}
			else {
				newKnowledgeSize++;
				int offset = offeringPageNum * 12;
				if (newKnowledgeSize > offset && newKnowledgeSize <= 12 + offset) {
					outerRing.add(item.asItemStack());
				}
			}
		}


		lastOfferingPage = (newKnowledgeSize - 1) / 12;
		// make sure offering page is within bounds
		if (offeringPageNum != 0) {
			if (offeringPageNum > lastOfferingPage)
				offeringPageNum = lastOfferingPage;
			else if (offeringPageNum < 0)
				offeringPageNum = 0;
		}
		
		ModServerToClientPackets.UPDATE_TRANSMUTATION_LAST_PAGE.send((ServerPlayerEntity)player, lastOfferingPage);
		
		// clear all the transmutation slots
		for (int i = 0; i < transmutationSlots.size(); i++)
			transmutationSlots.get(i).setStack(ItemStack.EMPTY);
		
		for (int i = 0; i < outerRing.size(); i++) {
			ItemStack stack = outerRing.get(i);
			transmutationSlots.get(i).setStack(stack);
		}
		for (int i = 0; i < innerRing.size(); i++) {
			ItemStack stack = innerRing.get(i);
			transmutationSlots.get(i + 12).setStack(stack);
		}
		return;
	}


	@Override
	public ItemStack quickMove(PlayerEntity player, int slotIndex) {
		if (slotIndex <= 2) {
			Slot slot = this.slots.get(slotIndex);
			if (!slot.hasStack())
				return ItemStack.EMPTY;
			ItemStack stack = slot.getStack();
			if (!this.insertItem(stack, playerSlotsStart, this.slots.size(), true)) 
				return ItemStack.EMPTY;
			return stack;
		}
		else if (slotIndex < 19) {
			TransmutationSlot slot = (TransmutationSlot)this.slots.get(slotIndex);
			if (!slot.hasStack())
				return ItemStack.EMPTY;
			ItemStack stack = slot.getStack().copy();
			
			SuperNumber itemEmc = EmcData.getItemEmc(stack.getItem());
			if (itemEmc.equalsZero())
				return ItemStack.EMPTY;

			SuperNumber emc;
			boolean client = player.getWorld().isClient();
			if (client)
				emc = FabricatedExchangeClient.clientEmc;
			else
				emc = EmcData.getEmc(player);
			
			SuperNumber itemCount = new SuperNumber(emc);
			itemCount.divide(itemEmc);
			itemCount.floor();
			
			SuperNumber sMaxCount = new SuperNumber(stack.getMaxCount());
			sMaxCount = SuperNumber.min(sMaxCount, itemCount);
			int a = sMaxCount.toInt(64);
			stack.setCount(a);

			SuperNumber itemCost = EmcData.getItemStackEmc(stack);

			if (emc.compareTo(itemCost) != -1) {
				if (!this.insertItem(stack, playerSlotsStart, this.slots.size(), true)) 
					return ItemStack.EMPTY;
				if (!client) {
					EmcData.subtractEmc((ServerPlayerEntity)player, itemCost);
					refreshOffering();
				}
				return stack;
			}
			
		
		}

		
		// And if it is not one any of the above slots it must mean we're shift clicking in the 
		// inventory, meaning transmute this item
		Slot slot = this.slots.get(slotIndex);
		ItemStack slotItemStack = slot.getStack();
		ItemStack itemStack = emcSlot.insertStack(slotItemStack, slotItemStack.getCount());
		if (!ItemStack.areEqual(slotItemStack, itemStack)) {
			slot.setStack(ItemStack.EMPTY);
		}
		
		return ItemStack.EMPTY; // we never actually want to move anything into the slot
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.inventory.canPlayerUse(player);
	}

	private void addTransmutationSlot(TransmutationSlot slot) {
		this.addSlot(slot);
		transmutationSlots.add(slot);
	}

	public DefaultedList<TransmutationSlot> getTransmutationSlots() {
		return transmutationSlots;
	}
}

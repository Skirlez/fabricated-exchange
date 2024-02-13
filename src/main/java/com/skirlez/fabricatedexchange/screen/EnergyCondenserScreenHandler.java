package com.skirlez.fabricatedexchange.screen;

import java.util.Optional;

import com.skirlez.fabricatedexchange.block.EnergyCondenserBlockEntity;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.slot.ConsiderateSlot;
import com.skirlez.fabricatedexchange.screen.slot.DisplaySlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SingleStackInventoryImpl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class EnergyCondenserScreenHandler extends LeveledScreenHandler implements ChestScreenHandler {
	private Inventory mainInventory;
	private Inventory targetInventory;
	private int size;

	private int playerSlotsStart;
	
	public static EnergyCondenserScreenHandler clientConstructor(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int level = buf.readInt();
		return new EnergyCondenserScreenHandler(syncId, playerInventory, 
				new SimpleInventory(EnergyCondenserBlockEntity.inventorySize(level)),
				new SingleStackInventoryImpl(), pos, level, Optional.of(buf));
	}

	
	public EnergyCondenserScreenHandler(int syncId, PlayerInventory playerInventory, 
			Inventory mainInventory, SingleStackInventory targetInventory, 
			BlockPos pos, int level, Optional<PacketByteBuf> buf) {
		
		super(ModScreenHandlers.ENERGY_CONDENSER, syncId, pos, level, buf);
		this.size = EnergyCondenserBlockEntity.inventorySize(level);
		checkSize(mainInventory, size);
		checkSize(targetInventory, 1);
		
		mainInventory.onOpen(playerInventory.player);
		this.addSlot(new DisplaySlot(targetInventory, 0, 12, 6));

		if (level == 0) {
			for (int i = 0; i < 7; i++) {
				for (int j = 0; j < 13; j++) 
					this.addSlot(new ConsiderateSlot(mainInventory, j + i * 13, 12 + j * 18, 26 + i * 18));
			}
		}
		else {
			for (int i = 0; i < 7; i++) {
				for (int j = 0; j < 6; j++) 
					this.addSlot(new ConsiderateSlot(mainInventory, j + i * 6, 12 + j * 18, 26 + i * 18));
			}

			for (int i = 0; i < 7; i++) {
				for (int j = 0; j < 6; j++) 
					this.addSlot(new ConsiderateSlot(mainInventory, 42 + j + i * 6, 138 + j * 18, 26 + i * 18));
			}
		}

		
		this.mainInventory = mainInventory;
		this.targetInventory = targetInventory;
		
		this.playerSlotsStart = slots.size();
		GeneralUtil.addPlayerInventory(this, playerInventory, 48, 154);
		GeneralUtil.addPlayerHotbar(this, playerInventory, 48, 212);
	}

	@Override
	public void onClosed(PlayerEntity player){
		super.onClosed(player);
		this.mainInventory.onClose(player);
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.mainInventory.canPlayerUse(player);
	}
	
	@Override
	public Inventory getInventory() {
		return this.mainInventory;
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slotIndex) {
		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasStack())
			return ItemStack.EMPTY;
		ItemStack stack = slot.getStack();
		
		if (slotIndex == 0) {
			DisplaySlot targetSlot = (DisplaySlot)slots.get(0);
			targetSlot.setStack(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		else if (slotIndex < playerSlotsStart) {
			if (!this.insertItem(stack, playerSlotsStart, slots.size(), true)) {
				return ItemStack.EMPTY;
			}
			return stack;
		}
		else {
			if (level == 0) {
				DisplaySlot targetSlot = (DisplaySlot)slots.get(0);
				targetSlot.setStack(stack.copyWithCount(1));
				return ItemStack.EMPTY;
			}
			else if (level == 1) {
				if (!this.insertItem(stack, 1, 43, false)) {
					return ItemStack.EMPTY;
				}
				return stack;
			}
		}
		return ItemStack.EMPTY;
		
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		super.onSlotClick(slotIndex, button, actionType, player);
		if (slotIndex == 0 && slots.get(slotIndex) instanceof DisplaySlot slot) {
			ItemStack cursorStack = getCursorStack();
			if (slot.hasStack() && !cursorStack.isEmpty()
					&& ItemStack.areItemsEqual(slot.getStack(), cursorStack)) {
				slot.setStackNoCallbacks(ItemStack.EMPTY);
				return;
			}
			if (cursorStack.isEmpty() || !EmcData.getItemEmc(cursorStack.getItem()).equalsZero()) {
				cursorStack = cursorStack.copy();
				cursorStack.setCount(1);
				slot.setStackNoCallbacks(cursorStack);
			}
  
		}
	}
	
	public ItemStack getTargetItemStack() {
		return targetInventory.getStack(0);
	}

}
package com.skirlez.fabricatedexchange.screen;

import java.util.Optional;

import com.skirlez.fabricatedexchange.block.EnergyCollectorBlockEntity;
import com.skirlez.fabricatedexchange.screen.slot.DisplaySlot;
import com.skirlez.fabricatedexchange.screen.slot.SlotCondition;
import com.skirlez.fabricatedexchange.screen.slot.SlotWithCondition;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.ModTags;
import com.skirlez.fabricatedexchange.util.SingleStackInventoryImpl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class EnergyCollectorScreenHandler extends FuelScreenHandler {

	private SingleStackInventory targetInventory;
	
	public static EnergyCollectorScreenHandler clientConstructor(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		int level = buf.readInt();
		return new EnergyCollectorScreenHandler(syncId, playerInventory, 
			new SimpleInventory(EnergyCollectorBlockEntity.inventorySize(level)), 
			new SingleStackInventoryImpl(), pos, level, Optional.of(buf));
	}
	
	public enum SlotIndicies {
		FUEL_SLOT,
		OUTPUT_SLOT,
		INPUT_SLOTS_START
	}
	
	public EnergyCollectorScreenHandler(int syncId, PlayerInventory playerInventory,
			Inventory mainInventory, SingleStackInventory targetInventory, BlockPos pos, int level, Optional<PacketByteBuf> buf) {
		super(ModScreenHandlers.ENERGY_COLLECTOR, syncId, mainInventory, pos, level, buf);
		
		this.targetInventory = targetInventory;
		mainInventory.onOpen(playerInventory.player);

		
		int xOffset;
		int invOffset;
		if (this.level == 0) {
			xOffset = 0;
			invOffset = 8;
		}
		else if (this.level == 1) {
			xOffset = 16;
			invOffset = 20;
		}
		else {
			xOffset = 34;
			invOffset = 30;
		}
		

		int inputOffset = (level == 0) ? 38 : 36 + level * 18;
		
		addSlot(new DisplaySlot(targetInventory, 0, xOffset + 153, 36));
		
		addSlot(new SlotWithCondition(mainInventory, SlotIndicies.FUEL_SLOT.ordinal(), xOffset + 124, 58, SlotCondition.isFuel));
		addSlot(new SlotWithCondition(mainInventory, SlotIndicies.OUTPUT_SLOT.ordinal(), xOffset + 124, 13, SlotCondition.never));
		
		int ind = 2;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2 + level; j++) {
				addSlot(new SlotWithCondition(mainInventory, ind, inputOffset - j * 18, 62 - i * 18, SlotCondition.isFuel));
				ind++;
			}
		}
		
		GeneralUtil.addPlayerInventory(this, playerInventory, invOffset, 84);
		GeneralUtil.addPlayerHotbar(this, playerInventory, invOffset, 142);
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		super.onSlotClick(slotIndex, button, actionType, player);
		if (slotIndex == 0) {
			DisplaySlot slot = (DisplaySlot)slots.get(0);
			ItemStack cursorStack = getCursorStack();
			if (slot.hasStack() && !cursorStack.isEmpty()
					&& ItemStack.areItemsEqual(slot.getStack(), cursorStack)) {
				slot.setStack(ItemStack.EMPTY);
				return;
			}
			if (cursorStack.isEmpty() || Registries.ITEM.getEntry(cursorStack.getItem()).streamTags().anyMatch(tag -> tag == ModTags.FUEL)) {
				cursorStack = cursorStack.copy();
				cursorStack.setCount(1);
				slot.setStack(cursorStack);
			}
		}
	}

	public ItemStack getTargetItemStack() {
		return this.targetInventory.getStack();
	}

}

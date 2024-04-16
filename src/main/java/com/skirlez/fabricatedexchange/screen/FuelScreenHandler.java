package com.skirlez.fabricatedexchange.screen;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;


// This is the parent class of EnergyCollectorScreenHandler and AntiMatterRelayScreenHandler, as they
// both share a considerable amount of code
public abstract class FuelScreenHandler extends LeveledScreenHandler {
	protected Inventory mainInventory;
	protected FuelScreenHandler(ScreenHandlerType<?> type, int syncId, Inventory mainInventory, BlockPos pos, int level, Optional<PacketByteBuf> buf) {
		super(type, syncId, pos, level, buf);
		this.mainInventory = mainInventory;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return player.squaredDistanceTo((double)pos.getX() + 0.5d, (double)pos.getY() + 0.5d, (double)pos.getZ() + 0.5d) 
				<= 64d;
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int invSlot) {
		ItemStack newStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(invSlot);
		if (slot != null && slot.hasStack()) {
			ItemStack originalStack = slot.getStack();
			newStack = originalStack.copy();
			if (invSlot < this.mainInventory.size()) {
				if (!insertItem(originalStack, this.mainInventory.size(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!insertItem(originalStack, 0, this.mainInventory.size(), false)) {
				return ItemStack.EMPTY;
			}

			if (originalStack.isEmpty())
				slot.setStack(ItemStack.EMPTY);
			else {
				slot.markDirty();
			}
		}

		return newStack;
	}

	public Inventory getInventory() {
		return mainInventory;
	}
}

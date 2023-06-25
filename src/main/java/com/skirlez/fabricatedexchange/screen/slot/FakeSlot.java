package com.skirlez.fabricatedexchange.screen.slot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

// This slot will not accept input from clicking. It is used as the target slot for the energy collectors.
public class FakeSlot extends Slot {
    public FakeSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }
    @Override
    public void setStack(ItemStack stack) { // TODO is this necessary
        return;
    }
}

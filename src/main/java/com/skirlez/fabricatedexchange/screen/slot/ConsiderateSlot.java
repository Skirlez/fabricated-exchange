package com.skirlez.fabricatedexchange.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

// This slot nicely asks the inventory if it is allowed to put a thing there
public class ConsiderateSlot extends Slot {

    public ConsiderateSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }
    @Override
    public boolean canInsert(ItemStack stack) {
        return inventory.isValid(getIndex(), stack);
    }
}

package com.skirlez.fabricatedexchange.screen.slot.collection;

import java.util.List;

import com.skirlez.fabricatedexchange.screen.slot.InputSlot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

// The output slot should attempt to move its contents to the input slots when possible.
// If the EMC of the target slot is greater or equal to the current item, don't move the item to the input slots.
public class OutputSlot extends Slot {
    private final List<InputSlot> inputSlots;
    public OutputSlot(Inventory inventory, int index, int x, int y, List<InputSlot> inputSlots) {
        super(inventory, index, x, y);
        this.inputSlots = inputSlots;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    public void moveToInputSlots() {
        if (!hasStack())
            return;
        for (int i = inputSlots.size() - 1; i >= 0; i--) {
            InputSlot slot = inputSlots.get(i);
            if (!slot.hasStack() || ItemStack.canCombine(slot.getStack(), getStack())) {
                this.setStack(slot.insertStack(getStack()));
            }
        }
    }

    
}

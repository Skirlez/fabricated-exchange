package com.skirlez.fabricatedexchange.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;


// Input slots should attempt to move their contents to the fuel slot when they can.
// This class differs from InputSlot, because fuelSlot also tries to move it's contents to the input slots,
// and that requires special consideration.
public class InputSlot extends Slot {
    private final FuelSlot fuelSlot;
    private final SlotCondition slotCondition;
    public InputSlot(Inventory inventory, int index, int x, int y, 
            FuelSlot fuelSlot, SlotCondition slotCondition) {
        super(inventory, index, x, y);
        this.fuelSlot = fuelSlot;
        this.slotCondition = slotCondition;
    }
    @Override
    public boolean canInsert(ItemStack stack) {
        return slotCondition.condition(stack);
    }
    @Override
    public void setStack(ItemStack stack) {
        super.setStack(stack);
        moveToFuelSlot();
    }
    public void moveToFuelSlot() {
        if (!fuelSlot.hasStack() || ItemStack.canCombine(fuelSlot.getStack(), getStack())) {
            setStackNoCallbacks(fuelSlot.insertStackNoCallbacks(getStack()));
        }
    }
}

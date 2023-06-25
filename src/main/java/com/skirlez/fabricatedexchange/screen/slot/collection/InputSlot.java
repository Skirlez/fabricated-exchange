package com.skirlez.fabricatedexchange.screen.slot.collection;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;


// Input slots should attempt to move their contents to the fuel slot when they can.
public class InputSlot extends Slot {
    private final FuelSlot fuelSlot;
    public InputSlot(Inventory inventory, int index, int x, int y, 
            FuelSlot fuelSlot) {
        super(inventory, index, x, y);
        this.fuelSlot = fuelSlot;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return FabricatedExchange.fuelProgressionMap.containsKey(stack.getItem());
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

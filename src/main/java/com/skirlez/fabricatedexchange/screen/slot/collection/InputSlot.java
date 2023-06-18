package com.skirlez.fabricatedexchange.screen.slot.collection;

import com.skirlez.fabricatedexchange.util.ModTags;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;

public class InputSlot extends Slot {

    private final FuelSlot fuelSlot;
    public InputSlot(Inventory inventory, int index, int x, int y, 
            FuelSlot fuelSlot) {
        super(inventory, index, x, y);
        this.fuelSlot = fuelSlot;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return Registries.ITEM.getEntry(stack.getItem()).streamTags().anyMatch(tag -> tag == ModTags.FUEL);
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

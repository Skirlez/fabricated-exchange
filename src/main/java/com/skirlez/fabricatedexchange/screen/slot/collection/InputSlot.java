package com.skirlez.fabricatedexchange.screen.slot.collection;

import com.skirlez.fabricatedexchange.util.ModTags;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;

public class InputSlot extends Slot {

    public InputSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return Registries.ITEM.getEntry(stack.getItem()).streamTags().anyMatch(tag -> tag == ModTags.FUEL);
    }
    
}

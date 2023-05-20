package com.skirlez.fabricatedexchange.screen.slot;

import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class TransmutationSlot extends Slot {
    // This slot will contain an item the player has learned. taking it will subtract from the player's EMC.
    private LivingEntity player;
    public TransmutationSlot(Inventory inventory, int index, int x, int y, LivingEntity player) {
        super(inventory, index, x, y);
        this.player = player;
    }

    // you are never supposed to be able to put things here
    public boolean canInsert(ItemStack stack) {
        return true;
    }

}

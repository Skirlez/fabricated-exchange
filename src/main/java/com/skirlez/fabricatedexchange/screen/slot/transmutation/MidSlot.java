package com.skirlez.fabricatedexchange.screen.slot.transmutation;

import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class MidSlot extends Slot {
    private TransmutationTableScreenHandler screenHandler;
    private boolean isClient;
    public MidSlot(Inventory inventory, int index, int x, int y, TransmutationTableScreenHandler screenHandler, boolean isClient) {
        super(inventory, index, x, y);
        this.screenHandler = screenHandler;
        this.isClient = isClient;
    }

    @Override
    public void setStack(ItemStack stack) {
        super.setStack(stack);
        if (!isClient)
            screenHandler.refreshOffering();
    }
}

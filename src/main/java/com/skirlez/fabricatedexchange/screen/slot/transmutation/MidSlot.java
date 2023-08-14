package com.skirlez.fabricatedexchange.screen.slot.transmutation;

import com.skirlez.fabricatedexchange.screen.TransmutationTableScreen;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;

import net.minecraft.client.MinecraftClient;
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
        else {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen instanceof TransmutationTableScreen screen)
                screen.resetAngleTime(1);
        }
    }
}

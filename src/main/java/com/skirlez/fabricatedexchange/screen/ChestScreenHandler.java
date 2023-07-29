package com.skirlez.fabricatedexchange.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public abstract class ChestScreenHandler extends ScreenHandler {
    protected Inventory inventory;

    protected ChestScreenHandler(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Override
    public void onClosed(PlayerEntity player){
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    public Inventory getInventory() {
        return this.inventory;
    }
}

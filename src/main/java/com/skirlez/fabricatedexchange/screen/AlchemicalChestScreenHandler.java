package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.util.GeneralUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class AlchemicalChestScreenHandler extends ScreenHandler implements ChestScreenHandler {
    protected Inventory inventory;
    public AlchemicalChestScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, new SimpleInventory(104));
    }

    public AlchemicalChestScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ModScreenHandlers.ALCHEMICAL_CHEST_SCREEN_HANDLER, syncId);
        checkSize(inventory, 104);
        this.inventory = inventory;
        inventory.onOpen(playerInventory.player);

        for(int i = 0; i < 8; i++) {
            for (int j = 0; j < 13; j++) 
                this.addSlot(new Slot(inventory, j + i * 13, 12 + j * 18, 5 + i * 18));
            
        }
        GeneralUtil.addPlayerInventory(this, playerInventory, 48, 152);
        GeneralUtil.addPlayerHotbar(this, playerInventory, 48, 210);
    }

    @Override
    public void close(PlayerEntity player){
        super.close(player);
        this.inventory.onClose(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
    
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot < 104) {
                if (!this.insertItem(itemStack2, 104, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } 
            else if (!this.insertItem(itemStack2, 0, 104, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty())
                slot2.setStack(ItemStack.EMPTY);
            else
                slot2.markDirty();
        }

        return itemStack;
    }


}
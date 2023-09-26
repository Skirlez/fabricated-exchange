package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.block.EnergyCondenserBlockEntity;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.slot.ConsiderateSlot;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class EnergyCondenserScreenHandler extends LeveledScreenHandler implements ChestScreenHandler {
    private Inventory inventory;
    private int size;
    public EnergyCondenserScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, buf.readBlockPos(), buf.readInt(), buf);
    }

    public EnergyCondenserScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos, int level, PacketByteBuf buf) {
        super(ModScreenHandlers.ENERGY_CONDENSER, syncId);
        EnergyCondenserBlockEntity blockEntity = (EnergyCondenserBlockEntity)playerInventory.player.getWorld().getBlockEntity(pos);
        this.pos = pos;
        this.level = level;
        this.size = (13 - level) * 7 + 1;
        if (blockEntity == null)
            this.inventory = new SimpleInventory(size);
        else {
            this.inventory = (Inventory)blockEntity;
            checkSize(inventory, size);
        }
        inventory.onOpen(playerInventory.player);
        this.buf = buf;
        this.addSlot(new FakeSlot(inventory, 0, 12, 6));

        if (level == 0) {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 13; j++) 
                    this.addSlot(new ConsiderateSlot(inventory, 1 + j + i * 13, 12 + j * 18, 26 + i * 18));
            }
        }
        else {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 6; j++) 
                    this.addSlot(new ConsiderateSlot(inventory, 1 + j + i * 6, 12 + j * 18, 26 + i * 18));
            }

            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 6; j++) 
                    this.addSlot(new ConsiderateSlot(inventory, 43 + j + i * 6, 138 + j * 18, 26 + i * 18));
            }
        }

        GeneralUtil.addPlayerInventory(this, playerInventory, 48, 154);
        GeneralUtil.addPlayerHotbar(this, playerInventory, 48, 212);
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
    
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            if (slot < size) {
                if (!this.insertItem(itemStack2, size, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (level == 1 && !this.insertItem(itemStack2, 1, 43, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty())
                slot2.setStack(ItemStack.EMPTY);
            else
                slot2.markDirty();
        }

        return itemStack;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);
        if (slotIndex == 0) {
            FakeSlot slot = (FakeSlot)slots.get(0);
            ItemStack cursorStack = getCursorStack();
            if (slot.hasStack() && !cursorStack.isEmpty()
                    && ItemStack.areItemsEqual(slot.getStack(), cursorStack)) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
                return;
            }
            if (cursorStack.isEmpty() || !EmcData.getItemEmc(cursorStack.getItem()).equalsZero()) {
                cursorStack = cursorStack.copy();
                cursorStack.setCount(1);
                slot.setStackNoCallbacks(cursorStack);
            }
  
        }
    }

}
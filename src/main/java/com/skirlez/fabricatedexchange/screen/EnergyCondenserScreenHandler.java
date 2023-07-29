package com.skirlez.fabricatedexchange.screen;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.block.EnergyCondenserBlockEntity;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class EnergyCondenserScreenHandler extends ChestScreenHandler {
    private final int level;
    private final BlockPos pos;
    private PacketByteBuf buf;
    public EnergyCondenserScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, (EnergyCondenserBlockEntity)inventory.player.getWorld().getBlockEntity(buf.readBlockPos()), buf);
    }

    public EnergyCondenserScreenHandler(int syncId, PlayerInventory playerInventory, EnergyCondenserBlockEntity blockEntity, PacketByteBuf buf) {
        super(ModScreenHandlers.ENERGY_CONDENSER_SCREEN_HANDLER, syncId);
        this.inventory = (Inventory)blockEntity;
        this.pos = blockEntity.getPos();
        this.level = blockEntity.getLevel();
        this.buf = buf;
        checkSize(inventory, (13 - level) * 7 + 1);
        inventory.onOpen(playerInventory.player);
        
        this.addSlot(new FakeSlot(inventory, 0, 12, 6));

        if (level == 0) {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 13; j++) 
                    this.addSlot(new Slot(inventory, 1 + j + i * 13, 12 + j * 18, 26 + i * 18));
            }
        }
        else {
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 6; j++) 
                    this.addSlot(new Slot(inventory, 1 + j + i * 6, 12 + j * 18, 26 + i * 18));
            }

            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < 6; j++) 
                    this.addSlot(new Slot(inventory, 43 + j + i * 6, 138 + j * 18, 26 + i * 18));
            }
        }

        GeneralUtil.addPlayerInventory(this, playerInventory, 48, 154);
        GeneralUtil.addPlayerHotbar(this, playerInventory, 48, 212);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
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


    // intended to be called by the screen instance
    @Nullable
    public PacketByteBuf getAndConsumeCreationBuffer() {
        if (buf == null)
            return null;
        PacketByteBuf copy = buf;
        buf = null;
        return copy;
    }


    public int getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }
}
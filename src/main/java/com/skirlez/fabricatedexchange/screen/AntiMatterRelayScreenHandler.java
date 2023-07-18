package com.skirlez.fabricatedexchange.screen;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.block.AntiMatterRelayBlockEntity;
import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.InputSlot;
import com.skirlez.fabricatedexchange.screen.slot.SlotCondition;
import com.skirlez.fabricatedexchange.util.GeneralUtil;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;




public class AntiMatterRelayScreenHandler extends ScreenHandler implements FuelScreenHandler {

    private final int level;
    private PacketByteBuf buf;
    private final Inventory inventory;
    private final DefaultedList<InputSlot> inputSlots = DefaultedList.of();
    private final BlockEntity blockEntity;
    public AntiMatterRelayScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(buf.readBlockPos()), buf.readInt());
        this.buf = buf;
    }
    public AntiMatterRelayScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, int level) {
        super(ModScreenHandlers.ANTIMATTER_RELAY_SCREEN_HANDLER, syncId);
        this.buf = null;
        this.level = level;
        this.inventory = (Inventory)blockEntity;
        FuelSlot fuelSlot = new FuelSlot(inventory, 0, 67, 38, (FuelScreenHandler)this, SlotCondition.alwaysTrue);
        Slot chargeSlot = new Slot(inventory, 1, 127, 38);
        addSlot(fuelSlot);
        addSlot(chargeSlot);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++)
                addInputSlot(new InputSlot(inventory, i * 2 + j + 2, 27 + j * 18, 12 + i * 18, fuelSlot, SlotCondition.alwaysTrue));
        }
        this.blockEntity = (AntiMatterRelayBlockEntity)blockEntity;
        GeneralUtil.addPlayerInventory(this, playerInventory, 8, 90);
        GeneralUtil.addPlayerHotbar(this, playerInventory, 8, 148);
    }

    public int getLevel() {
        return level;
    }

    // intended to be called by the screen instance
    @Nullable
    public PacketByteBuf getAndConsumeCreationBuffer() {
        if (buf == null)
            return null;
        PacketByteBuf copy = new PacketByteBuf(buf.copy());
        buf = null;
        return copy;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addInputSlot(InputSlot slot) {
        this.addSlot(slot);
        inputSlots.add(slot);
    }
    
    public void moveAllInputsToFuel() {
        for (int i = inputSlots.size() - 1; i >= 0; i--)
            inputSlots.get(i).moveToFuelSlot();
    }

    public FuelSlot getFuelSlot() {
        return (FuelSlot)slots.get(0);
    }
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

}

package com.skirlez.fabricatedexchange.screen;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.screen.slot.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.InputSlot;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;


// This is the parent class of EnergyCollectorScreenHandler and AntiMatterRelayScreenHandler, as they
// both share a considerable amount of code
public abstract class FuelScreenHandler extends ScreenHandler {
    protected final Inventory inventory;
    protected final BlockEntity blockEntity;
    protected final int level;
    protected PacketByteBuf buf;
    protected DefaultedList<InputSlot> inputSlots = DefaultedList.of();
    protected FuelScreenHandler(ScreenHandlerType<?> type, int syncId, BlockEntity blockEntity, int level, PacketByteBuf buf) {
        super(type, syncId);
        this.blockEntity = blockEntity;
        this.inventory = (Inventory)blockEntity;
        this.level = level;
        this.buf = buf;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        // if we don't do this, shift-clicking the fuel slot will not only transfer anything in it
        // to the player inventory, but also any item of the same type in all of the input slots
        // this is because the act of moving it to the inventory immediately updates the input slots which
        // causes them to move to the fuel slot, and the code responsible for shift-clicking runs in a while loop until
        // the slot is empty. so we try to check for this case and forcefully return empty to get out of the while loop
        Slot slot = this.slots.get(invSlot);
        ItemStack startStack = slot.getStack().copy();
        if (slot instanceof FuelSlot) {
            FuelSlot fuelSlot = (FuelSlot)slot;
            if (fuelSlot.hasQuickMoved()) { // The Case has been detected, return empty
                fuelSlot.setQuickMoved(false);
                return ItemStack.EMPTY;
            }
        }
        ItemStack result = quickMoveInternal(player, invSlot);
        if (slot instanceof FuelSlot) {
            FuelSlot fuelSlot = (FuelSlot)slot;
            if (result.isEmpty())
                return ItemStack.EMPTY;
            if (fuelSlot.hasStack() && ItemStack.areItemsEqual(startStack, fuelSlot.getStack()))
                fuelSlot.setQuickMoved(true);
        }
        return result;
    }

    private ItemStack quickMoveInternal(PlayerEntity player, int invSlot) {
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

            if (originalStack.isEmpty())
                slot.setStack(ItemStack.EMPTY);
            else {
                slot.markDirty();
            }
        }

        return newStack;
    }
    public FuelSlot getFuelSlot() {
        return (FuelSlot)slots.get(0);
    }
    public void moveAllInputsToFuel() {
        for (int i = inputSlots.size() - 1; i >= 0; i--)
            inputSlots.get(i).moveToFuelSlot();
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

    public BlockEntity getBlockEntity() {
        return blockEntity;
    }
    public int getLevel() {
        return level;
    }


}

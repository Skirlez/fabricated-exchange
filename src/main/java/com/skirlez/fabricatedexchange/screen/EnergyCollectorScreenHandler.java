package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.block.EnergyCollectorBlockEntity;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.FuelSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.InputSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.OutputSlot;
import com.skirlez.fabricatedexchange.util.ModTags;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

public class EnergyCollectorScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final EnergyCollectorBlockEntity blockEntity;
    private final DefaultedList<InputSlot> inputSlots = DefaultedList.of();
    private final int outputIndex;
    private final int level;
    public EnergyCollectorScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(buf.readBlockPos()), buf.readInt());
    }
    public EnergyCollectorScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity, int level) {
        super(ModScreenHandlers.ENERGY_COLLECTOR_SCREEN_HANDLER, syncId);
        this.inventory = (Inventory)blockEntity;
        this.level = level;
        checkSize(inventory, 11 + this.level * 4);

        int inputOffset;
        int otherOffset;
        if (this.level == 0) {
            inputOffset = 0;
            otherOffset = 0;
        }
        else if (this.level == 1) {
            inputOffset = -14;
            otherOffset = 4;
        }
        else {
            inputOffset = -24;
            otherOffset = 12;
        }

        this.blockEntity = (EnergyCollectorBlockEntity)blockEntity;
        inventory.onOpen(playerInventory.player);
        FuelSlot fuelSlot = new FuelSlot(inventory, 0, otherOffset + 124, 58, this);
        addSlot(fuelSlot);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2 + level; j++)
                addInputSlot(new InputSlot(inventory, i * 2 + j + 1, inputOffset + 20 + j * 18, 8 + i * 18, fuelSlot));
        }



        outputIndex = slots.size();
        addSlot(new OutputSlot(inventory, outputIndex, otherOffset + 124, 13, inputSlots));

        addSlot(new FakeSlot(inventory, outputIndex + 1, otherOffset + 153, 36));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
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

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);
        if (slotIndex == outputIndex + 1) {
            FakeSlot slot = (FakeSlot)slots.get(outputIndex + 1);
            ItemStack cursorStack = getCursorStack();
            if (slot.hasStack() && !cursorStack.isEmpty()
                    && ItemStack.areItemsEqual(slot.getStack(), cursorStack)) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
                return;
            }
            if (cursorStack.isEmpty() || Registries.ITEM.getEntry(cursorStack.getItem()).streamTags().anyMatch(tag -> tag == ModTags.FUEL)) {
                cursorStack = cursorStack.copy();
                cursorStack.setCount(1);
                slot.setStackNoCallbacks(cursorStack);
            }
        }
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

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }


    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    private void addInputSlot(InputSlot slot) {
        this.addSlot(slot);
        inputSlots.add(slot);
    }
    
    public void moveAllInputsToFuel() {
        for (int i = inputSlots.size() - 1; i >= 0; i--)
            inputSlots.get(i).moveToFuelSlot();
        ((OutputSlot)slots.get(outputIndex)).moveToInputSlots();
    }

    // accessing the slots list at this index should always give you the output slot,
    // and accessing the slots list at this index + 1 should always give you the target slot
    public int getOutputSlotIndex() {
        return outputIndex;
    }

    public EnergyCollectorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public int getLevel() {
        return level;
    }

}

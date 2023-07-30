package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.block.EnergyCollectorBlockEntity;
import com.skirlez.fabricatedexchange.screen.slot.FakeSlot;
import com.skirlez.fabricatedexchange.screen.slot.collection.OutputSlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.ModTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class EnergyCollectorScreenHandler extends FuelScreenHandler {
    public EnergyCollectorScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, (EnergyCollectorBlockEntity)inventory.player.getWorld().getBlockEntity(buf.readBlockPos()), buf.readInt(), buf);
    }
    public EnergyCollectorScreenHandler(int syncId, PlayerInventory playerInventory, EnergyCollectorBlockEntity blockEntity, int level, PacketByteBuf buf) {
        super(ModScreenHandlers.ENERGY_COLLECTOR_SCREEN_HANDLER, syncId, blockEntity, level, buf);
        inventory.onOpen(playerInventory.player);

        addSlot(blockEntity.getFuelSlot());
        addSlot(blockEntity.getOutputSlot());
        addSlot(blockEntity.getTargetSlot());

        inputSlots = blockEntity.getInputSlots();
        for (int i = 0; i < inputSlots.size(); i++) {
            addSlot(inputSlots.get(i));
        }
        int invOffset;
        if (this.level == 0) 
            invOffset = 8;
        else if (this.level == 1) 
            invOffset = 20;
        else 
            invOffset = 30;
        

        GeneralUtil.addPlayerInventory(this, playerInventory, invOffset, 84);
        GeneralUtil.addPlayerHotbar(this, playerInventory, invOffset, 142);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        super.onSlotClick(slotIndex, button, actionType, player);
        if (slotIndex == 2) {
            FakeSlot slot = (FakeSlot)slots.get(2);
            ItemStack cursorStack = getCursorStack();
            if (slot.hasStack() && !cursorStack.isEmpty()
                    && ItemStack.areItemsEqual(slot.getStack(), cursorStack)) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
                ((OutputSlot)slots.get(1)).moveToInputSlots();
                return;
            }
            if (cursorStack.isEmpty() || Registries.ITEM.getEntry(cursorStack.getItem()).streamTags().anyMatch(tag -> tag == ModTags.FUEL)) {
                cursorStack = cursorStack.copy();
                cursorStack.setCount(1);
                slot.setStackNoCallbacks(cursorStack);
                ((OutputSlot)slots.get(1)).moveToInputSlots();
            }
        }
    }

    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        ItemStack itemStack;
        Slot slot;
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }
        if (stack.isStackable()) {
            while (!stack.isEmpty() && (fromLast ? i >= startIndex : i < endIndex)) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();

                // the last condition is the only difference between the super method and this, amazing
                // I added it so you wouldn't be able to shift click to add items to the output slot and target slot
                // I would use a mixin but I'm afraid of breaking something for other mods
                if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack) && slot.canInsert(stack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxCount()) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.markDirty();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxCount()) {
                        stack.decrement(stack.getMaxCount() - itemStack.getCount());
                        itemStack.setCount(stack.getMaxCount());
                        slot.markDirty();
                        bl = true;
                    }
                }
                if (fromLast) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        if (!stack.isEmpty()) {
            i = fromLast ? endIndex - 1 : startIndex;
            while (fromLast ? i >= startIndex : i < endIndex) {
                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (itemStack.isEmpty() && slot.canInsert(stack)) {
                    if (stack.getCount() > slot.getMaxItemCount()) {
                        slot.setStack(stack.split(slot.getMaxItemCount()));
                    } else {
                        slot.setStack(stack.split(stack.getCount()));
                    }
                    slot.markDirty();
                    bl = true;
                    break;
                }
                if (fromLast) {
                    --i;
                    continue;
                }
                ++i;
            }
        }
        return bl;
    }

}

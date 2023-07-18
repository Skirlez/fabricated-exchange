package com.skirlez.fabricatedexchange.screen.slot;

import com.skirlez.fabricatedexchange.screen.FuelScreenHandler;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;


public class FuelSlot extends Slot {
    private boolean quickMoved;
    private final FuelScreenHandler screenHandler;
    private final SlotCondition slotCondition;
    public FuelSlot(Inventory inventory, int index, int x, int y, FuelScreenHandler screenHandler, SlotCondition slotCondition) {
        super(inventory, index, x, y);
        this.screenHandler = screenHandler;
        this.slotCondition = slotCondition;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return slotCondition.condition(stack);
    }

    @Override
    public void setStack(ItemStack stack) {
        super.setStack(stack);
        screenHandler.moveAllInputsToFuel();
    }

    @Override
    public ItemStack takeStack(int amount) {
        ItemStack result = super.takeStack(amount);
        screenHandler.moveAllInputsToFuel();
        return result;
    }

    public ItemStack insertStackNoCallbacks(ItemStack stack) {
        if (stack.isEmpty() || !this.canInsert(stack)) {
            return stack;
        }
        int count = stack.getCount();
        ItemStack itemStack = this.getStack();
        int i = Math.min(Math.min(count, stack.getCount()), this.getMaxItemCount(stack) - itemStack.getCount());
        if (itemStack.isEmpty()) {
            setStackNoCallbacks(stack.split(i));
        } else if (ItemStack.canCombine(itemStack, stack)) {
            stack.decrement(i);
            itemStack.increment(i);
            setStackNoCallbacks(itemStack);
        }
        return stack;
    }
    
    public boolean hasQuickMoved() {
        return quickMoved;
    }
    public void setQuickMoved(boolean quickMoved) {
        this.quickMoved = quickMoved;
    }
}

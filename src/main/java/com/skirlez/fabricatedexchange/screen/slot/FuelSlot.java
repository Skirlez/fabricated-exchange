package com.skirlez.fabricatedexchange.screen.slot;

import java.util.List;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;


public class FuelSlot extends Slot {
    private boolean quickMoved;
    private final SlotCondition slotCondition;
    private final List<InputSlot> inputSlots;
    public FuelSlot(Inventory inventory, int index, int x, int y, List<InputSlot> inputSlots, SlotCondition slotCondition) {
        super(inventory, index, x, y);
        this.slotCondition = slotCondition;
        this.inputSlots = inputSlots;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return slotCondition.condition(stack);
    }

    @Override
    public void setStack(ItemStack stack) {
        super.setStack(stack);
        moveAllInputsToFuel();
        
    }

    private void moveAllInputsToFuel() {
        for (int i = 0; i < inputSlots.size(); i++)
            inputSlots.get(i).moveToFuelSlot();
    }

    @Override
    public ItemStack takeStack(int amount) {
        ItemStack result = super.takeStack(amount);
        moveAllInputsToFuel();
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

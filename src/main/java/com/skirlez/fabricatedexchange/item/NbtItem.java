package com.skirlez.fabricatedexchange.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

// A simple class that holds an item with NBT data and nothing else.
public class NbtItem implements ItemConvertible {
    private final Item item;
    private final NbtCompound nbt;

    public NbtItem(Item item) {
        this.item = item;
        this.nbt = null;
    }

    public NbtItem(Item item, NbtCompound nbt) {
        this.item = item;
        this.nbt = nbt.copy();
    }


    public NbtItem(ItemStack stack) {
        this.item = stack.getItem();
        
        NbtCompound nbt = stack.getNbt();
        this.nbt = (nbt == null) ? null : nbt.copy();
    }

    @Override
    public Item asItem() {
        return item;
    }
    
    public ItemStack asItemStack() {
        ItemStack stack = new ItemStack(item, 1);
        stack.setNbt(nbt);
        return stack;
    }

    public NbtCompound getNbt() {
        return nbt;
    }

    public boolean equalTo(NbtItem other) {
        if (!item.equals(other.asItem()))
            return false;
        if (nbt == null) {
            if (other.nbt == null)
                return true;
            return false;
        }
        if (nbt.equals(other.getNbt()))
            return true;
        return false;
    }
}

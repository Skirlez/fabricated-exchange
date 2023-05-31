package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.Item;

@Mixin(Item.class)
public interface ItemAccessor {
    @Mutable
    @Accessor
    void setRecipeRemainder(Item item);
}

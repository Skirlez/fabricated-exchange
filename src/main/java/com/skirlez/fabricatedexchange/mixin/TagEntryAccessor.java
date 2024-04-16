package com.skirlez.fabricatedexchange.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.TagKey;


@Mixin(Ingredient.TagEntry.class)
public interface TagEntryAccessor  {
    @Accessor
    public TagKey<Item> getTag();

}
package com.skirlez.fabricatedexchange.util;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static final TagKey<Item> FUEL = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "transmutation_fuel"));

}

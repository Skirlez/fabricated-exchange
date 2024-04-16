package com.skirlez.fabricatedexchange.util;

import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList.Named;

public class ModTags {
	public static final TagKey<Item> STONE = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "stone"));
	public static final TagKey<Item> FUEL = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "transmutation_fuel"));
	public static final Optional<Named<Item>> FUEL_ITEMS = Registry.ITEM.getEntryList(ModTags.FUEL); // On datagen, this is an empty optional
}

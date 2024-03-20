package com.skirlez.fabricatedexchange.util;

import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
	public static final TagKey<Item> STONE = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "stone"));
	public static final TagKey<Item> FUEL = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "transmutation_fuel"));
	public static final Optional<Named<Item>> FUEL_ITEMS = Registries.ITEM.getEntryList(ModTags.FUEL); // On datagen, this is an empty optional
}

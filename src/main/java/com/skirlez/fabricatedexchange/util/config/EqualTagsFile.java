package com.skirlez.fabricatedexchange.util.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

public class EqualTagsFile extends DataFile<HashSet<String>> {

	private HashMap<Item, Integer> tagGroups;
	private int groupAmount;
	public EqualTagsFile(String name) {
		super(new TypeReference<HashSet<String>>() {}, name);
		tagGroups = new HashMap<Item, Integer>();
		groupAmount = 0;
	}


	@Override
	protected void constProcess() {
		super.constProcess();
		tagGroups.clear();
		for (String entry : value) {

			if (entry == null)
				continue;
			if (!entry.contains(":"))
				continue;
			Item[] items = GeneralUtil.getItemsFromTagString(entry);
			if (items.length == 0) {
				FabricatedExchange.LOGGER.warn("Item-less tag provided in equal_tags.json: " + entry + ". Ignoring...");
				continue;
			}
			for (Item item : items) {
				tagGroups.put(item, groupAmount);
			}
			groupAmount++;
		}
	}

	private int getGroup(Item item) {
		return tagGroups.containsKey(item) ? tagGroups.get(item) : -1;
	}

	public boolean hasTag(TagKey<Item> tag) {
		Item[] items = GeneralUtil.getItemsFromTag(tag, Registries.ITEM);
		boolean hasFirst = tagGroups.containsKey(items[0]);
		if (items.length == 0 || hasFirst == false)
			return hasFirst;
		int group = getGroup(items[0]);
		for (int i = 1; i < items.length; i++) {
			if (getGroup(items[i]) != group)
				return false;
		}
		return true;
	}

	/** Returns a list of all the equal item groups (lists). */
	public List<List<Item>> getItemGroups() {
		List<List<Item>> itemGroups = new ArrayList<List<Item>>(groupAmount);
		for (int i = 0; i < groupAmount; i++)
			itemGroups.add(i, new ArrayList<Item>());

		for (Item item : tagGroups.keySet())
			itemGroups.get(tagGroups.get(item)).add(item);

		return itemGroups;
	}

}

package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.reflect.TypeToken;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

public class NbtItemsFile extends DataFile<Map<String, List<String>>>  {

	public NbtItemsFile(String name) {
		super(new TypeReference<Map<String, List<String>>>() {}, name);
	}

	HashMap<String, String> itemsToTag = new HashMap<String, String>();

	@Override
	protected void constProcess() {
		super.constProcess();
		itemsToTag.clear();
		Iterator<String> iterator = value.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (!key.startsWith("#"))
				continue;
			key = key.substring(1);
			// tag found
			String[] items = GeneralUtil.getItemStringsFromTagString(key);
            if (items.length == 0) {
                FabricatedExchange.LOGGER.warn("Item-less tag provided in nbt_items.json: " + key + ". Ignoring...");
                continue;
            }
			for (int i = 0; i < items.length; i++) {
				itemsToTag.put(items[i], key);
			}
		}
	}
	public boolean hasItem(String idName) {
		if (value.containsKey(idName))
			return true;
		String tag = itemsToTag.get(idName);
		if (tag == null)
			return false;
		return value.containsKey("#" + tag);
	}

	public List<String> getAllowedKeys(String idName) {
		if (value.containsKey(idName))
			return value.get(idName);
		else {
			String tag = itemsToTag.get(idName);
			if (tag == null) {
				FabricatedExchange.LOGGER.error("Item " + idName + " does not have an entry in nbt_items.json!");
				return null;
			}
			return value.get("#" + tag);
		}
	}
	
}

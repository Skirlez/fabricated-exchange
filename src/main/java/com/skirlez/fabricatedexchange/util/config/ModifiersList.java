package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

public class ModifiersList extends DataFile<HashSet<String>> {

	public ModifiersList(Type type, String name) {
		super(type, name);
		tagModifiers = new HashSet<String>();
	}

	private HashSet<String> tagModifiers;

	@Override
	protected void constProcess() {
		super.constProcess();
		tagModifiers.clear();
		Iterator<String> iterator = value.iterator();
		while (iterator.hasNext()) {
			String entry = iterator.next();
			if (!entry.startsWith("#"))
				continue;
			entry = entry.substring(1);
			String[] items = GeneralUtil.getItemStringsFromTagString(entry);
            if (items.length == 0) {
                FabricatedExchange.LOGGER.warn("Item-less tag provided in modifiers.json: " + entry + ". Ignoring...");
                continue;
            }
			for (int i = 0; i < items.length; i++) {
				tagModifiers.add(items[i]);
			}
		}
	}

	public boolean hasItem(String item) {
		return value.contains(item) || tagModifiers.contains(item);
	}
}
package com.skirlez.fabricatedexchange.util.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import com.google.gson.reflect.TypeToken;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class EmcMapFile extends DataFile<Map<String, String>> {
	private Map<Item, SuperNumber> emcMap;
	
	public EmcMapFile(String name) {
		super(new TypeToken<Map<String, String>>() {}, name);
		emcMap = null;
	}
	@Override
	protected void constProcess() {
		super.constProcess();
		Map<Item, SuperNumber> map = new HashMap<Item, SuperNumber>();
		for (Entry<String, String> entry : value.entrySet()) {
			String itemName = entry.getKey();
			Identifier id = new Identifier(itemName);
			Item item = Registries.ITEM.get(id);
			if (item == null)
				continue;
			
			String num = entry.getValue();
			if (!SuperNumber.isValidNumberString(num)) {
				FabricatedExchange.LOGGER.warn("Invalid number string " + num + " for item " + itemName + " in file " + this.name + ".");
				continue;
			}
			map.put(item, new SuperNumber(num));
		}
		emcMap = Collections.unmodifiableMap(map);
	}
	
	public Map<Item, SuperNumber> getEmcMap() {
		return emcMap;
	}
	public boolean hasItem(Item item) {
		return emcMap.containsKey(item);
	}

}

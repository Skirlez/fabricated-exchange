package com.skirlez.fabricatedexchange.util.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.lib.AbstractConfigFile;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;


/** The central config file of the mod. It will compare itself to the default config to make sure the config file is not broken, and "repairs" itself if necessary. */
public class MainConfig extends AbstractConfigFile<Map<String, Object>> {
	
	private final Map<String, Object> defaultValue;
	
	MainConfig(String name) {
		super(Map.class, name);
		defaultValue = copyDefaultValue();
	}

	public boolean showItemEmcOrigin;
	public boolean showEnchantedBookRepairCost;
	public SuperNumber enchantmentEmcConstant;
	public SuperNumber emcInMultiplier;
	public SuperNumber emcOutMultiplier;
	public boolean mapper_enabled;
	public boolean transmutationTable_animated;
	public boolean transmutationTable_floorButton;


	/* Fetch the config and compare to the default config to see if any keys
	are missing or any of them have mismatched value types. If true, set that 
	key to the default value and save */
	
	@Override
	protected Map<String, Object> process(Map<String, Object> value) {
		value = super.process(value);
		
		if (defaultValue == null)
			return value;
		
		boolean changed = false;
		if (value == null)
			value = new LinkedHashMap<String, Object>();
		for (String key : defaultValue.keySet()) {
			if (!value.containsKey(key)) {
				FabricatedExchange.LOGGER.info("Config file is missing key " + key + "! Inserting it into the config with the default value.");
				value.put(key, defaultValue.get(key));
				if (!changed)
					changed = true;
			}
			else if (!value.get(key).getClass().equals(defaultValue.get(key).getClass())) {
				FabricatedExchange.LOGGER.info("Key " + key + " has different value type than the default type! Reseting it to the default value.");
				value.put(key, defaultValue.get(key));
				if (!changed)
					changed = true;
			}
		}
		
		if (!FabricatedExchange.VERSION.equals("${version}")) { // dev env check
			if (!value.get("version").equals(FabricatedExchange.VERSION)) {
				// any version migration code would go here
				value.put("version", FabricatedExchange.VERSION);
				changed = true;
			}
		}
			
		if (changed)
			save();

		return value;
	}
	@Override
	protected void constProcess() {
		super.constProcess();
		showItemEmcOrigin = (boolean)value.get("showItemEmcOrigin");
		showEnchantedBookRepairCost = (boolean)value.get("showEnchantedBookRepairCost");
		emcInMultiplier = new SuperNumber((String)value.get("emcInMultiplier"));
		emcOutMultiplier = new SuperNumber((String)value.get("emcOutMultiplier"));
		enchantmentEmcConstant = new SuperNumber((String)value.get("enchantmentEmcConstant"));
		mapper_enabled = (boolean)value.get("mapper.enabled");
		transmutationTable_animated = (boolean)value.get("transmutationTable.animated");
		transmutationTable_floorButton = (boolean)value.get("transmutationTable.floorButton");
		
	}
	
	@Environment(EnvType.CLIENT)
	public void updateComments() {
		ImmutableMap.Builder<String, String[]> commentsBuilder = ImmutableMap.builder();
		for (String key : defaultValue.keySet()) {
			List<Text> comments = GeneralUtil.translatableList("config.fabricated-exchange." + key);
			String[] arr = new String[comments.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = comments.get(i).getString();
			}
			commentsBuilder.put(key, arr);
		}
		ImmutableMap<String, String[]> newMap = commentsBuilder.build();
		if (!areCommentsMapEqual(newMap, commentsMap)) {
			commentsMap = newMap;
			save();
		}
		

	}
	
	public boolean areCommentsMapEqual(Map<String, String[]> map1, Map<String, String[]> map2) {
		for (Map.Entry<String, String[]> entry : map1.entrySet()) {
			if (!map2.containsKey(entry.getKey())) 
				return false;
			String[] arr1 = entry.getValue();
			String[] arr2 = map2.get(entry.getKey());
			if (arr1.length != arr2.length)
				return false;
			for (int i = 0; i < arr1.length; i++) {
				if (!arr1[i].equals(arr2[i]))
					return false;
			}
		}
		return true;
	}
	
	public Optional<Class<?>> getKeyClass(String key) {
		if (!defaultValue.containsKey(key))
			return Optional.empty();
		return Optional.of(defaultValue.get(key).getClass());
	}
	
}


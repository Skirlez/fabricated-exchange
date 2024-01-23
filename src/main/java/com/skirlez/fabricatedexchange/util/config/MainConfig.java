package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.lib.ConfigFile;


/** The central config file of the mod. It will compare itself to the default config to make sure the config file is not broken, and "repairs" itself if necessary. */
public class MainConfig extends ConfigFile<Map<String, Object>> {
	
	private final Map<String, Object> defaultValue = copyDefaultValue();
	
	MainConfig(Type type, String name) {
		super(type, name);
		addComments("version", "Do not modify this string!");
		addComments("showItemEmcOrigin", "Show how the mod decided this item's EMC value.", "Will not be accurate on multiplayer servers. Useful when manually setting EMC values!",
		"(default: false)");
		addComments("showEnchantedBookRepairCost", "Show the hidden repair cost attribute for Enchanted Books.", 
		"(default: true)");
		addComments("enchantmentEmcConstant", "A special constant used during the calculation of enchantment EMC.", "Bigger constant -> more EMC.",
		"(default: 3260)");
		addComments("emcInMultiplier", "The amount of personal EMC you gain be multiplied by this number.",
		"(default: 1)");
		addComments("emcOutMultiplier", "The amount of personal EMC you need to spend will be multiplied by this number.",
		"(default: 1)");
		addComments("mapper.enabled", "Whether or not the EMC mapper is enabled.",
		"(default: true)");
		addComments("transmutationTable.animated", "When disabled, the Transmutation Table will look boring.",
		"(default: true)");
		addComments("transmutationTable.floorButton", "When enabled, a button to round down your EMC will appear", "in the transmutation table when your EMC is not a whole number.",
		"(default: true)");

	}

	public boolean showItemEmcOrigin;
	public boolean showEnchantedBookRepairCost;
	public boolean mapper_enabled;
	public boolean transmutationTable_animated;
	public boolean transmutationTable_floorButton;
	public SuperNumber emcInMultiplier;
	public SuperNumber emcOutMultiplier;
	public SuperNumber enchantmentEmcConstant;

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
	
	public Optional<Class<?>> getKeyClass(String key) {
		if (!defaultValue.containsKey(key))
			return Optional.empty();
		return Optional.of(defaultValue.get(key).getClass());
	}
	
}


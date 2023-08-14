package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.DataFile;

public class ConfigFile extends DataFile<Map<String, Object>> {
    ConfigFile(Type type, String name) {
        super(type, name);
    }

    public boolean showItemEmcOrigin;
    public boolean showEnchantedBookRepairCost;
    public boolean mapper_enabled;
    public boolean transmutationTable_animated;
    public boolean transmutationTable_floorButton;

    public static enum Bool {
        SHOW_ITEM_EMC_ORIGIN,
        MAPPER_ENABLED,
        TRANSMUTATION_TABLE_ANIMATED
    }

    /* Fetch the config and compare to the default config to see if any keys
    are missing or any of them have mismatched value types. If true, set that 
    key to the default value and save */
    @Override
    public void fetch() {
        super.fetch();
        
        Map<String, Object> defaultValue = getDefaultValue();
        boolean changed = false;
        if (value != null) {
            Iterator<String> iterator = defaultValue.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (!value.containsKey(key)) {
                    FabricatedExchange.LOGGER.error("Config file is missing key " + key + "! Inserting it into the config with the default value.");
                    value.put(key, defaultValue.get(key));
                    if (!changed)
                        changed = true;
                }
                else if (!value.get(key).getClass().equals(defaultValue.get(key).getClass())) {
                    FabricatedExchange.LOGGER.error("Key " + key + " has different value type than the default type! Reseting it to the default value.");
                    value.put(key, defaultValue.get(key));
                    if (!changed)
                        changed = true;
                }
            }
        }
        save();

        showItemEmcOrigin = (boolean)value.get("showItemEmcOrigin");
        showEnchantedBookRepairCost = (boolean)value.get("showEnchantedBookRepairCost");
        mapper_enabled = (boolean)value.get("mapper.enabled");
        transmutationTable_animated = (boolean)value.get("transmutationTable.animated");
        transmutationTable_floorButton = (boolean)value.get("transmutationTable.floorButton");
    }
}


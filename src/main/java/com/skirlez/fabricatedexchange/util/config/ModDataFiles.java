package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;


public class ModDataFiles {
    private ModDataFiles() {
    
    }
    
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(FabricatedExchange.MOD_ID);

    // trust me this is safe. bro trust me
    @SuppressWarnings("unchecked")
    private static final Type jsonType = (Class<LinkedHashMap<String, Object>>)(Class<?>)LinkedHashMap.class;

    private static final Type emcMapType = new TypeToken<HashMap<String, SuperNumber>>() {}.getType();
    private static final Type stringSetType = new TypeToken<HashSet<String>>() {}.getType();
    private static final Type nbtItemsType = new TypeToken<HashMap<String, List<String>>>() {}.getType();
    private static final Type recipeBlacklistType = new TypeToken<HashMap<String, HashSet<String>>>() {}.getType();
    private static final Type blockTransmutationMapType = new TypeToken<String[][]>() {}.getType();

    public static final ModConfig CONFIG_FILE = 
        new ModConfig(jsonType, "config.yaml");
        
    public static final DataFile<Map<String, SuperNumber>> SEED_EMC_MAP 
        = new SeedEmcMap(emcMapType, "seed_emc_map.json");

    public static final DataFile<Map<String, SuperNumber>> CUSTOM_EMC_MAP
        = new CustomEmcMap(emcMapType, "custom_emc_map.json");

    public static final DataFile<Map<String, SuperNumber>> ENCHANTMENT_EMC_MAP
        = new DataFile<Map<String, SuperNumber>>(emcMapType, "enchantment_emc_map.json");

    public static final DataFile<String[][]> BLOCK_TRANSMUTATION_MAP
        = new DataFile<String[][]>(blockTransmutationMapType,
        "block_transmutation_map.json");

    public static final ModifiersList MODIFIERS
        = new ModifiersList(stringSetType, "modifiers.json");

    public static final NbtItemsList NBT_ITEMS = 
        new NbtItemsList(nbtItemsType, "nbt_items.json");

    public static final DataFile<Map<String, HashSet<String>>> BLACKLISTED_MAPPER_RECIPES
        = new DataFile<Map<String, HashSet<String>>>(recipeBlacklistType,
        "blacklisted_mapper_recipes.json");

    public static void fetchAll() {
        CONFIG_FILE.fetch();
        SEED_EMC_MAP.fetch();
        CUSTOM_EMC_MAP.fetch();
        ENCHANTMENT_EMC_MAP.fetch();
        BLOCK_TRANSMUTATION_MAP.fetch();
        MODIFIERS.fetch();
        NBT_ITEMS.fetch();
        BLACKLISTED_MAPPER_RECIPES.fetch();
    }
}

class EmcMap extends DataFile<Map<String, SuperNumber>> {
    public EmcMap(Type type, String name) {
        super(type, name);
    }
    @Override
    protected void process() {
        List<Pair<String, SuperNumber>> pairs = new ArrayList<Pair<String, SuperNumber>>();
        Iterator<String> iterator = value.keySet().iterator();
        while (iterator.hasNext()) {
            String entry = iterator.next();
            if (!entry.startsWith("#"))
                continue;
            entry = entry.substring(1);
            String[] items = GeneralUtil.getItemStringsFromTagString(entry);
            SuperNumber emc = value.get("#" + entry);
            for (int i = 0; i < items.length; i++)
                pairs.add(new Pair<String, SuperNumber>(items[i], emc));

            iterator.remove();
        }

        for (int i = 0; i < pairs.size(); i++) {
            Pair<String, SuperNumber> pair = pairs.get(i);
            value.put(pair.getLeft(), pair.getRight());
        }
    }
}

class SeedEmcMap extends EmcMap {
    public SeedEmcMap(Type type, String name) {
        super(type, name);
    }
    @Override
    protected void process() {
        super.process();
        EmcData.seedEmcMap = this.value;
    }
}
class CustomEmcMap extends EmcMap {
    public CustomEmcMap(Type type, String name) {
        super(type, name);
    }
    @Override
    protected void process() {
        super.process();
        EmcData.customEmcMap = this.value;
    }
}



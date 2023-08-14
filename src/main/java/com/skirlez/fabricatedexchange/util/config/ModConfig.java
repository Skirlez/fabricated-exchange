package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.DataFile;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;


public class ModConfig {
    private ModConfig() {
    
    }
    
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(SuperNumber.class, (JsonSerializer<SuperNumber>)(superNumber, type, jsonSerializationContext) 
        -> new JsonPrimitive(superNumber.divisionString()))
        .registerTypeAdapter(SuperNumber.class, (JsonDeserializer<SuperNumber>) (jsonElement, type, jsonDeserializationContext) 
        -> new SuperNumber(jsonElement.getAsString()))
        .setPrettyPrinting() // we do want these to be editable by users
        .create();

    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(FabricatedExchange.MOD_ID);

    private static final Type jsonType = new TypeToken<HashMap<String, Object>>() {}.getType();
    private static final Type emcMapType = new TypeToken<HashMap<String, SuperNumber>>() {}.getType();
    private static final Type stringSetType = new TypeToken<HashSet<String>>() {}.getType();
    private static final Type nbtItemsType = new TypeToken<HashMap<String, List<String>>>() {}.getType();
    private static final Type recipeBlacklistType = new TypeToken<HashMap<String, HashSet<String>>>() {}.getType();
    private static final Type blockTransmutationMapType = new TypeToken<String[][]>() {}.getType();

    public static final ConfigFile CONFIG_FILE = new ConfigFile(jsonType, 
        "config.json");

    public static final DataFile<Map<String, SuperNumber>> SEED_EMC_MAP_FILE 
        = new SeedEmcMap(emcMapType, 
        "seed_emc_map.json");

    public static final DataFile<Map<String, SuperNumber>> CUSTOM_EMC_MAP_FILE
        = new CustomEmcMap(emcMapType, 
        "custom_emc_map.json");

    public static final DataFile<HashSet<String>> MODIFIERS
        = new DataFile<HashSet<String>>(stringSetType, 
        "modifiers.json");

    public static final NbtItemsFile NBT_ITEMS = new NbtItemsFile(nbtItemsType, "nbt_items.json");

    public static final DataFile<Map<String, HashSet<String>>> BLACKLISTED_MAPPER_RECIPES_FILE
        = new DataFile<Map<String, HashSet<String>>>(recipeBlacklistType,
        "blacklisted_mapper_recipes.json");

    public static final DataFile<String[][]> BLOCK_TRANSMUTATION_MAP_FILE
        = new DataFile<String[][]>(blockTransmutationMapType,
        "block_transmutation_map.json");


    public static void fetchAll() {
        CONFIG_FILE.fetch();
        SEED_EMC_MAP_FILE.fetch();
        CUSTOM_EMC_MAP_FILE.fetch();
        MODIFIERS.fetch();
        NBT_ITEMS.fetch();
        BLACKLISTED_MAPPER_RECIPES_FILE.fetch();
        BLOCK_TRANSMUTATION_MAP_FILE.fetch();
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
            // tag found
            iterator.remove();

            String[] parts = entry.split(":");
            if (parts.length != 2)
                return;
            Identifier tagId = new Identifier(parts[0], parts[1]);
            TagKey<Item> tag = Registries.ITEM.streamTags().filter((key) -> (key.id().equals(tagId))).findFirst().orElse(null);
            if (tag == null) {
                FabricatedExchange.LOGGER.error("Invalid tag in " + name + "! Tag: " + tagId.toString());
                continue;
            }
            
            SuperNumber emc = value.get(entry);
            Iterator<RegistryEntry<Item>> itemIterator = Registries.ITEM.getEntryList(tag).get().iterator();
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next().value();
                pairs.add(new Pair<String, SuperNumber>(Registries.ITEM.getId(item).toString(), emc));
            }
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



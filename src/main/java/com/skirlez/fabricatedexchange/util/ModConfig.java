package com.skirlez.fabricatedexchange.util;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.loader.api.FabricLoader;


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

    private static Type jsonType = new TypeToken<HashMap<String, Object>>() {}.getType();
    private static Type emcMapType = new TypeToken<HashMap<String, SuperNumber>>() {}.getType();
    private static Type recipeBlacklistType = new TypeToken<HashMap<String, String[]>>() {}.getType();
    private static Type blockTransmutationMapType = new TypeToken<String[][]>() {}.getType();

    public static final DataFile<Map<String, Object>> CONFIG_FILE 
        = new DataFile<Map<String, Object>>(jsonType, 
        "config.json");

    public static final DataFile<Map<String, SuperNumber>> SEED_EMC_MAP_FILE 
        = new DataFile<Map<String, SuperNumber>>(emcMapType, 
        "seed_emc_map.json");

    public static final DataFile<Map<String, SuperNumber>> CUSTOM_EMC_MAP_FILE
        = new DataFile<Map<String, SuperNumber>>(emcMapType, 
        "custom_emc_map.json");

    public static final DataFile<Map<String, String[]>> BLACKLISTED_MAPPER_RECIPES_FILE
        = new DataFile<Map<String, String[]>>(recipeBlacklistType,
        "blacklisted_mapper_recipes.json");

    public static final DataFile<String[][]> BLOCK_TRANSMUTATION_MAP_FILE
        = new DataFile<String[][]>(blockTransmutationMapType,
        "block_transmutation_map.json");


    public static void fetchAll() {
        CONFIG_FILE.fetch();
        SEED_EMC_MAP_FILE.fetch();
        CUSTOM_EMC_MAP_FILE.fetch();
        BLACKLISTED_MAPPER_RECIPES_FILE.fetch();
        BLOCK_TRANSMUTATION_MAP_FILE.fetch();
    }
}

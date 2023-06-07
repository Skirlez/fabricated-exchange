package com.skirlez.fabricatedexchange.util;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.LinkedTreeMap;
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
        .setPrettyPrinting()
        .create();

    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(FabricatedExchange.MOD_ID);

    private static Type jsonType = new TypeToken<HashMap<String, Object>>() {}.getType();
    private static Type emcMapType = new TypeToken<HashMap<String, SuperNumber>>() {}.getType();
    private static Type recipeBlacklistType = new TypeToken<HashMap<String, String[]>>() {}.getType();
    private static Type blockTransmutationMapType = new TypeToken<String[][]>() {}.getType();

    public static final DataFile<HashMap<String, Object>> CONFIG_FILE 
        = new DataFile<HashMap<String, Object>>(jsonType, 
        "config.json");

    public static final DataFile<HashMap<String, SuperNumber>> SEED_EMC_MAP_FILE 
        = new DataFile<HashMap<String, SuperNumber>>(emcMapType, 
        "seed_emc_map.json");

    public static final DataFile<HashMap<String, SuperNumber>> CUSTOM_EMC_MAP_FILE
        = new DataFile<HashMap<String, SuperNumber>>(emcMapType, 
        "custom_emc_map.json");

    public static final DataFile<HashMap<String, String[]>> BLACKLISTED_MAPPER_RECIPES_FILE
        = new DataFile<HashMap<String, String[]>>(recipeBlacklistType,
        "blacklisted_mapper_recipes.json");

    public static final DataFile<String[][]> BLOCK_TRANSMUTATION_MAP_FILE
        = new DataFile<String[][]>(blockTransmutationMapType,
        "block_transmutation_map.json");



}

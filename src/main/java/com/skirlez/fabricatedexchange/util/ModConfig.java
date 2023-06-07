package com.skirlez.fabricatedexchange.util;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
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
        .setPrettyPrinting()
        .create();

    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(FabricatedExchange.MOD_ID);

    private static Type configType = new TypeToken<HashMap<String, Object>>() {}.getType();
    private static Type emcMapType = new TypeToken<HashMap<String, SuperNumber>>() {}.getType();

    public static final DataFile<HashMap<String, Object>> CONFIG_FILE 
        = new DataFile<HashMap<String, Object>>(configType, 
        CONFIG_DIR.resolve("config.json"), 
        "Config file");

    public static final DataFile<HashMap<String, SuperNumber>> SEED_EMC_MAP_FILE 
        = new DataFile<HashMap<String, SuperNumber>>(emcMapType, 
        CONFIG_DIR.resolve("seed_emc_map.json"), 
        "Seed EMC map file");

    public static final DataFile<HashMap<String, SuperNumber>> CUSTOM_EMC_MAP_FILE
        = new DataFile<HashMap<String, SuperNumber>>(emcMapType, 
        CONFIG_DIR.resolve("custom_emc_map.json"),
        "Custom EMC map file");
}

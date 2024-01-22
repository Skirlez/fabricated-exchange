package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;


public class ModDataFiles {

	private ModDataFiles() {
	
	}
	
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(FabricatedExchange.MOD_ID);

	// trust me this is safe. bro trust me
	@SuppressWarnings("unchecked")
	private static final Type jsonType = (Class<LinkedHashMap<String, Object>>)(Class<?>)LinkedHashMap.class;
	
	private static final Type emcMapType = new TypeToken<HashMap<Item, SuperNumber>>() {}.getType();
	
	private static final Type stringSetType = new TypeToken<HashSet<String>>() {}.getType();
	private static final Type nbtItemsType = new TypeToken<HashMap<String, List<String>>>() {}.getType();
	private static final Type recipeBlacklistType = new TypeToken<HashMap<String, HashSet<String>>>() {}.getType();
	private static final Type blockTransmutationMapType = new TypeToken<String[][]>() {}.getType();

	public static final MainConfig MAIN_CONFIG_FILE = 
		new MainConfig(jsonType, "config.yaml");
		
	public static final DataFile<Map<Item, SuperNumber>> SEED_EMC_MAP 
		= new DataFile<Map<Item, SuperNumber>>(emcMapType, "seed_emc_map.json");

	public static final DataFile<Map<Item, SuperNumber>> CUSTOM_EMC_MAP
		= new DataFile<Map<Item, SuperNumber>>(emcMapType, "custom_emc_map.json");

	//public static final DataFile<Map<Enchantment, SuperNumber>> ENCHANTMENT_EMC_MAP
	//	= new DataFile<Map<Enchantment, SuperNumber>>(emcMapType, "enchantment_emc_map.json");

	public static final DataFile<String[][]> BLOCK_TRANSMUTATION_MAP
		= new DataFile<String[][]>(blockTransmutationMapType,
		"block_transmutation_map.json");

    public static final EqualTagsFile EQUAL_TAGS
        = new EqualTagsFile(stringSetType, "equal_tags.json");
		
	public static final ModifiersList MODIFIERS
		= new ModifiersList(stringSetType, "modifiers.json");

	public static final NbtItemsList NBT_ITEMS = 
		new NbtItemsList(nbtItemsType, "nbt_items.json");

	public static final DataFile<Map<String, HashSet<String>>> BLACKLISTED_MAPPER_RECIPES
		= new DataFile<Map<String, HashSet<String>>>(recipeBlacklistType,
		"blacklisted_mapper_recipes.json");

	public static void fetchAll() {
		MAIN_CONFIG_FILE.fetch();
		SEED_EMC_MAP.fetch();
		CUSTOM_EMC_MAP.fetch();
		//ENCHANTMENT_EMC_MAP.fetch();
		BLOCK_TRANSMUTATION_MAP.fetch();
		MODIFIERS.fetch();
		NBT_ITEMS.fetch();
		EQUAL_TAGS.fetch();
		BLACKLISTED_MAPPER_RECIPES.fetch();
	}
}



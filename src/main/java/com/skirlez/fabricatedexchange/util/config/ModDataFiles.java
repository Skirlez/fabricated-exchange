package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.config.lib.AbstractFile;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

import net.fabricmc.loader.api.FabricLoader;


public class ModDataFiles {

	private ModDataFiles() {
	
	}
	
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(FabricatedExchange.MOD_ID);

	// trust me this is safe. bro trust me
	@SuppressWarnings("unchecked")
	private static final Type jsonType = (Class<LinkedHashMap<String, Object>>)(Class<?>)LinkedHashMap.class;
	
	private static final Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
	
	private static final Type stringSetType = new TypeToken<HashSet<String>>() {}.getType();
	private static final Type nbtItemsType = new TypeToken<HashMap<String, List<String>>>() {}.getType();
	private static final Type recipeBlacklistType = new TypeToken<HashMap<String, HashSet<String>>>() {}.getType();
	private static final Type blockTransmutationMapType = new TypeToken<String[][]>() {}.getType();

	public static final MainConfig MAIN_CONFIG_FILE = 
		new MainConfig(jsonType, "config.yaml");
		
	public static final EmcMapFile SEED_EMC_MAP 
		= new EmcMapFile(mapType, "seed_emc_map.json");

	public static final EmcMapFile CUSTOM_EMC_MAP
		= new EmcMapFile(mapType, "custom_emc_map.json");

	//public static final DataFile<Map<Enchantment, SuperNumber>> ENCHANTMENT_EMC_MAP
	//	= new DataFile<Map<Enchantment, SuperNumber>>(emcMapType, "enchantment_emc_map.json");

	public static final DataFile<String[][]> BLOCK_TRANSMUTATION_MAP
		= new DataFile<String[][]>(blockTransmutationMapType,
		"block_transmutation_map.json");

    public static final EqualTagsFile EQUAL_TAGS
        = new EqualTagsFile(stringSetType, "equal_tags.json");

	public static final NbtItemsFile NBT_ITEMS = 
		new NbtItemsFile(nbtItemsType, "nbt_items.json");

	public static final DataFile<Map<String, HashSet<String>>> BLACKLISTED_MAPPER_RECIPES
		= new DataFile<Map<String, HashSet<String>>>(recipeBlacklistType,
		"blacklisted_mapper_recipes.json");

	public static final List<AbstractFile<?>> ALL_FILES = Arrays.asList(
			MAIN_CONFIG_FILE, SEED_EMC_MAP, CUSTOM_EMC_MAP,
			BLOCK_TRANSMUTATION_MAP, EQUAL_TAGS,
			NBT_ITEMS, BLACKLISTED_MAPPER_RECIPES);
	
	public static void fetchAll() {
		for (AbstractFile<?> file : ALL_FILES)
			file.fetch();
	}

	public static Optional<AbstractFile<?>> getFileByName(String name) {
		for (AbstractFile<?> file : ALL_FILES) {
			if (file.getName().equals(name))
				return Optional.of(file);
		}
		return Optional.empty();
	}
}



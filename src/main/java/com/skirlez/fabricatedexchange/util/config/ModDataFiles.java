package com.skirlez.fabricatedexchange.util.config;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.config.lib.AbstractFile;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

import net.fabricmc.loader.api.FabricLoader;


public class ModDataFiles {

	private ModDataFiles() {
	
	}
	
	public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(FabricatedExchange.MOD_ID);


	public static final MainConfig MAIN_CONFIG_FILE 
		= new MainConfig("config.yaml");
		
	public static final EmcMapFile SEED_EMC_MAP 
		= new EmcMapFile("seed_emc_map.json");

	public static final EmcMapFile CUSTOM_EMC_MAP
		= new EmcMapFile("custom_emc_map.json");

	//public static final DataFile<Map<Enchantment, SuperNumber>> ENCHANTMENT_EMC_MAP
	//	= new DataFile<Map<Enchantment, SuperNumber>>(emcMapType, "enchantment_emc_map.json");

	public static final DataFile<String[][]> BLOCK_TRANSMUTATION_MAP
		= new DataFile<String[][]>(new TypeReference<String[][]>() {}, "block_transmutation_map.json");

    public static final EqualTagsFile EQUAL_TAGS
        = new EqualTagsFile("equal_tags.json");

	public static final NbtItemsFile NBT_ITEMS
		= new NbtItemsFile("nbt_items.json");

	public static final BlacklistedRecipesFile BLACKLISTED_MAPPER_RECIPES
		= new BlacklistedRecipesFile("blacklisted_mapper_recipes.json");

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



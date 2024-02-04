package com.skirlez.fabricatedexchange.util.config;

import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

public class BlacklistedRecipesFile extends DataFile<Map<String, HashSet<String>>> {
	public BlacklistedRecipesFile(String name) {
		super(new TypeReference<Map<String, HashSet<String>>>() {}, name);
	}
	
	public boolean isRecipeBlacklisted(String recipeId, String type) {
		return (value.containsKey(type) && value.get(type).contains(recipeId));
	}

}

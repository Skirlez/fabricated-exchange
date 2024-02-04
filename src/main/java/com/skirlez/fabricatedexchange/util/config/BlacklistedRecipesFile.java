package com.skirlez.fabricatedexchange.util.config;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSet;
import com.google.gson.reflect.TypeToken;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

public class BlacklistedRecipesFile extends DataFile<Map<String, List<String>>> {
	private ImmutableSet<String> blacklist;
	public BlacklistedRecipesFile(String name) {
		super(new TypeToken<Map<String, List<String>>>() {}, name);
		blacklist = ImmutableSet.of();
	}
	
	@Override
	protected void constProcess() {
		super.constProcess();
		ImmutableSet.Builder<String> set = ImmutableSet.builder();
		for (Map.Entry<String, List<String>> entry : value.entrySet()) {
			String type = entry.getKey();
			for (String recipeId : entry.getValue())
				set.add(type + ":" + recipeId);
			
		}
		blacklist = set.build();
	}
	
	public boolean isRecipeBlacklisted(String recipeId, String type) {
		return blacklist.contains(type + ":" + recipeId);
	}

}

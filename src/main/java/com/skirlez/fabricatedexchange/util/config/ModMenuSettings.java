package com.skirlez.fabricatedexchange.util.config;

import com.skirlez.fabricatedexchange.screen.SettingsScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screen.Screen;

public class ModMenuSettings implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return new ConfigScreenFactory<Screen>() {
			@Override
			public Screen create(Screen parent) {
				return new SettingsScreen(parent);
			}
		};
	}
}

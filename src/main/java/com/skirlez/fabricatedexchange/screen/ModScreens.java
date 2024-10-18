package com.skirlez.fabricatedexchange.screen;

import net.minecraft.client.gui.screen.ingame.HandledScreens;

public final class ModScreens {
	private ModScreens() {}

	public static void register() {
		HandledScreens.register(ModScreenHandlers.TRANSMUTATION_TABLE, TransmutationTableScreen::new);
		HandledScreens.register(ModScreenHandlers.ENERGY_COLLECTOR, EnergyCollectorScreen::new);
		HandledScreens.register(ModScreenHandlers.ANTIMATTER_RELAY, AntiMatterRelayScreen::new);
		HandledScreens.register(ModScreenHandlers.ALCHEMICAL_CHEST, AlchemicalChestScreen::new);
		HandledScreens.register(ModScreenHandlers.ENERGY_CONDENSER, EnergyCondenserScreen::new);
		HandledScreens.register(ModScreenHandlers.MERCURIAL_EYE, MercurialEyeScreen::new);
	}
}

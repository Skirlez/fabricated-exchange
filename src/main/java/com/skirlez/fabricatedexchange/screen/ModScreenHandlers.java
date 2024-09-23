package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;


public final class ModScreenHandlers {
	private ModScreenHandlers() {}

	public static final ScreenHandlerType<TransmutationTableScreenHandler> TRANSMUTATION_TABLE = 
		new ExtendedScreenHandlerType<TransmutationTableScreenHandler>(TransmutationTableScreenHandler::clientConstructor);
	public static final ScreenHandlerType<EnergyCollectorScreenHandler> ENERGY_COLLECTOR = 
		new ExtendedScreenHandlerType<EnergyCollectorScreenHandler>(EnergyCollectorScreenHandler::clientConstructor);
	public static final ScreenHandlerType<AntiMatterRelayScreenHandler> ANTIMATTER_RELAY = 
		new ExtendedScreenHandlerType<AntiMatterRelayScreenHandler>(AntiMatterRelayScreenHandler::clientConstructor);
	public static final ScreenHandlerType<AlchemicalChestScreenHandler> ALCHEMICAL_CHEST = 
		new ScreenHandlerType<AlchemicalChestScreenHandler>(AlchemicalChestScreenHandler::new, FeatureSet.empty());
	public static final ScreenHandlerType<EnergyCondenserScreenHandler> ENERGY_CONDENSER = 
		new ExtendedScreenHandlerType<>(EnergyCondenserScreenHandler::clientConstructor);


	public static void register() {
		// TODO figure out what the Strings are for
		Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID,
			"transmutation_table"), TRANSMUTATION_TABLE);
		
		Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID,
			"energy_collector"), ENERGY_COLLECTOR);
		
		Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID,
			"antimatter_relay"), ANTIMATTER_RELAY);
		
		Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID,
			"alchemical_chest"), ALCHEMICAL_CHEST);
		
		Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID,
			"energy_condenser"), ENERGY_CONDENSER);
	}
}

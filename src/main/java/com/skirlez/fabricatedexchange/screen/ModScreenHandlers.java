package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;


public class ModScreenHandlers {
    public static ScreenHandlerType<TransmutationTableScreenHandler> TRANSMUTATION_TABLE_SCREEN_HANDLER = 
        new ScreenHandlerType<>(TransmutationTableScreenHandler::new, FeatureSet.empty());

    public static ScreenHandlerType<EnergyCollectorScreenHandler> ENERGY_COLLECTOR_SCREEN_HANDLER = 
        new ExtendedScreenHandlerType<>(EnergyCollectorScreenHandler::new);

    public static ScreenHandlerType<AntiMatterRelayScreenHandler> ANTIMATTER_RELAY_SCREEN_HANDLER = 
        new ExtendedScreenHandlerType<>(AntiMatterRelayScreenHandler::new);

    public static ScreenHandlerType<AlchemicalChestScreenHandler> ALCHEMICAL_CHEST_SCREEN_HANDLER = 
        new ScreenHandlerType<>(AlchemicalChestScreenHandler::new, FeatureSet.empty());

    public static void registerAllScreenHandlers() {
        // TODO figure out what the strings are for
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "transmutation_table"),
            TRANSMUTATION_TABLE_SCREEN_HANDLER);
            
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "energy_collector"),
            ENERGY_COLLECTOR_SCREEN_HANDLER);

        Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "antimatter_relay"),
            ANTIMATTER_RELAY_SCREEN_HANDLER);
        
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "alchemical_chest"),
            ALCHEMICAL_CHEST_SCREEN_HANDLER);
    }
}

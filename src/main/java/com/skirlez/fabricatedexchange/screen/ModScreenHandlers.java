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

    public static void registerAllScreenHandlers() {
        // TODO figure out what the strings are for
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "transmutation"),
            TRANSMUTATION_TABLE_SCREEN_HANDLER);
            
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "emc_collection"),
            ENERGY_COLLECTOR_SCREEN_HANDLER);
    }
}

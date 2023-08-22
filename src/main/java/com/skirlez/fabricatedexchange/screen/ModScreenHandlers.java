package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.FabricatedExchange;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;


public class ModScreenHandlers {
    public static final ScreenHandlerType<TransmutationTableScreenHandler> TRANSMUTATION_TABLE_SCREEN_HANDLER = 
        new ExtendedScreenHandlerType<>(TransmutationTableScreenHandler::new);

    public static final ScreenHandlerType<EnergyCollectorScreenHandler> ENERGY_COLLECTOR_SCREEN_HANDLER = 
        new ExtendedScreenHandlerType<>(EnergyCollectorScreenHandler::new);

    public static final ScreenHandlerType<AntiMatterRelayScreenHandler> ANTIMATTER_RELAY_SCREEN_HANDLER = 
        new ExtendedScreenHandlerType<>(AntiMatterRelayScreenHandler::new);

    public static final ScreenHandlerType<AlchemicalChestScreenHandler> ALCHEMICAL_CHEST_SCREEN_HANDLER = 
        new ScreenHandlerType<>(AlchemicalChestScreenHandler::new);

    public static final ScreenHandlerType<EnergyCondenserScreenHandler> ENERGY_CONDENSER_SCREEN_HANDLER = 
        new ExtendedScreenHandlerType<>(EnergyCondenserScreenHandler::new);


    public static void registerAllScreenHandlers() {
        // TODO figure out what the strings are for
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "transmutation_table"),
            TRANSMUTATION_TABLE_SCREEN_HANDLER);
            
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "energy_collector"),
            ENERGY_COLLECTOR_SCREEN_HANDLER);

        Registry.register(Registry.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "antimatter_relay"),
            ANTIMATTER_RELAY_SCREEN_HANDLER);
        
        Registry.register(Registry.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "alchemical_chest"),
            ALCHEMICAL_CHEST_SCREEN_HANDLER);

        Registry.register(Registry.SCREEN_HANDLER, new Identifier(FabricatedExchange.MOD_ID, "energy_condenser"),
            ENERGY_CONDENSER_SCREEN_HANDLER);
    }
}

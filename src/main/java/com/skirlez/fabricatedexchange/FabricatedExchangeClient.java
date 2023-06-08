package com.skirlez.fabricatedexchange;
import com.skirlez.fabricatedexchange.event.KeyInputHandler;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreen;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
public class FabricatedExchangeClient implements ClientModInitializer {
    public static SuperNumber clientEmc = SuperNumber.Zero();
    

    @Override
    public void onInitializeClient() {
        
        HandledScreens.register(ModScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER, TransmutationTableScreen::new);
        ModMessages.registerS2CPackets();
        

        KeyInputHandler.register();
    }
}

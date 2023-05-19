package com.skirlez.fabricatedexchange;

import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreen;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.nbt.NbtCompound;


public class FabricatedExchangeClient implements ClientModInitializer {
    public static NbtCompound nbt;


    @Override
    public void onInitializeClient() {

        HandledScreens.register(ModScreenHandlers.TRANSMUTATION_TABLE_SCREEN_HANDLER, TransmutationTableScreen::new);
        ModMessages.registerS2CPackets();
    }
}
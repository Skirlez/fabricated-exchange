package com.skirlez.fabricatedexchange.networking;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.networking.packet.*;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ModMessages {
    public static final Identifier EMC_USE_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "emc_use");
    public static final Identifier ITEM_CHARGE_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "charge_item");
    public static final Identifier EMC_SYNC_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "emc_sync");
    public static final Identifier EMC_MAP_SYNC_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "emc_map_sync");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ITEM_CHARGE_IDENTIFIER, ItemChargeC2SPacket::receive);

        
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(EMC_SYNC_IDENTIFIER, EmcSyncS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(EMC_MAP_SYNC_IDENTIFIER, EmcMapSyncS2CPacket::receive);

    }
}

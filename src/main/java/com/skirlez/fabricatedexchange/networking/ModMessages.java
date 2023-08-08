package com.skirlez.fabricatedexchange.networking;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.networking.packet.*;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ModMessages {
    public static final Identifier TRANSMUTATION_TABLE_WIDGETS = new Identifier(FabricatedExchange.MOD_ID, "transmutation_table_widgets");
    public static final Identifier ITEM_CHARGE_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "charge_item");
    public static final Identifier EXTRA_FUNCTION_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "extra_function");
    public static final Identifier FLOOR_EMC_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "floor_emc");

    public static final Identifier CONSUMER_BLOCK_SYNC = new Identifier(FabricatedExchange.MOD_ID, "antimatter_relay_sync");
    public static final Identifier EMC_SYNC_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "emc_sync");
    public static final Identifier EMC_MAP_SYNC_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "emc_map_sync");
    public static final Identifier BLOCK_TRANSMUTATION_SYNC_IDENTIFIER = new Identifier(FabricatedExchange.MOD_ID, "block_transmutation_sync");
    public static final Identifier TRANSMUTATION_TABLE_MAX_PAGE = new Identifier(FabricatedExchange.MOD_ID, "transmutation_max_page");
    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ITEM_CHARGE_IDENTIFIER, ItemChargeC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(EXTRA_FUNCTION_IDENTIFIER, ExtraFunctionItemC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(FLOOR_EMC_IDENTIFIER, FloorEmcC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(TRANSMUTATION_TABLE_WIDGETS, TransmutationTableWidgetsC2SPacket::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(EMC_SYNC_IDENTIFIER, EmcSyncS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(EMC_MAP_SYNC_IDENTIFIER, EmcMapSyncS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(CONSUMER_BLOCK_SYNC, ConsumerBlockSyncS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(TRANSMUTATION_TABLE_MAX_PAGE, TransmutationTableMaxPageS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(BLOCK_TRANSMUTATION_SYNC_IDENTIFIER, BlockTransmutationSyncS2CPacket::receive);
    }
}

package com.skirlez.fabricatedexchange.packets;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public abstract class ModServerToClientPackets {
	
	public static final UpdateConsumerBlock UPDATE_CONSUMER_BLOCK
		= new UpdateConsumerBlock("update_consumer_block");
	public static final UpdateTransmutationLastPage UPDATE_TRANSMUTATION_LAST_PAGE
		= new UpdateTransmutationLastPage("update_transmutation_last_page");
	public static final UpdatePlayerEmc UPDATE_PLAYER_EMC
		= new UpdatePlayerEmc("update_player_emc");
	public static final UpdateEmcMaps UPDATE_EMC_MAPS
		= new UpdateEmcMaps("update_emc_maps");
	public static final UpdateBlockTransmutationMap UPDATE_BLOCK_TRANSMUTATION_MAP
		= new UpdateBlockTransmutationMap("update_block_transmutation_map");
	
	
	public static final ServerToClientPacket[] PACKETS = {
			UPDATE_CONSUMER_BLOCK, 
			UPDATE_TRANSMUTATION_LAST_PAGE, UPDATE_PLAYER_EMC,
			UPDATE_EMC_MAPS, UPDATE_BLOCK_TRANSMUTATION_MAP};

	public static void register() {
		for (ServerToClientPacket packet : PACKETS) {
			registerPacket(packet);
		}
	}
	private static <T extends ServerToClientPacket> void registerPacket(T packet) {
		ClientPlayNetworking.registerGlobalReceiver(packet.getId(), packet::receive);
	}
}

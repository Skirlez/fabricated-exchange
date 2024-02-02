package com.skirlez.fabricatedexchange.packets;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public abstract class ModServerToClientPackets {
	
	public static final UpdateConsumerBlock UPDATE_CONSUMER_BLOCK = registerPacket(
		new UpdateConsumerBlock("update_consumer_block"));
	public static final UpdateTransmutationLastPage UPDATE_TRANSMUTATION_LAST_PAGE = registerPacket(
		new UpdateTransmutationLastPage("update_transmutation_last_page"));
	public static UpdatePlayerEmc UPDATE_PLAYER_EMC = registerPacket(
		new UpdatePlayerEmc("update_player_emc"));
	public static UpdateEmcMaps UPDATE_EMC_MAPS = registerPacket(
		new UpdateEmcMaps("update_emc_maps"));
	public static UpdateBlockTransmutationMap UPDATE_BLOCK_TRANSMUTATION_MAP = registerPacket(
		new UpdateBlockTransmutationMap("update_block_transmutation_map"));
	

	public static void register() {

	}
	private static <T extends ServerToClientPacket> T registerPacket(T packet) {
		ClientPlayNetworking.registerGlobalReceiver(packet.getId(), packet::receive);
		return packet;
	}
}

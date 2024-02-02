package com.skirlez.fabricatedexchange.packets;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public abstract class ModClientToServerPackets {
	
	public static final ChargeItem CHARGE_ITEM = registerPacket(
			new ChargeItem("charge_item"));
	public static final CycleItemMode CYCLE_ITEM_MODE = registerPacket(
			new CycleItemMode("cycle_item_mode"));
	public static final FloorEmc FLOOR_EMC = registerPacket(
			new FloorEmc("floor_emc"));
	public static final DoItemExtraFunction DO_ITEM_EXTRA_FUNCTION = registerPacket(
			new DoItemExtraFunction("do_item_extra_function"));
	public static final UpdateTransmutationWidget UPDATE_TRANSMUTATION_WIDGET = registerPacket(
			new UpdateTransmutationWidget("update_transmutation_widget"));

	public static void register() {
		
	}
	
	private static <T extends ClientToServerPacket> T registerPacket(T packet) {
		ServerPlayNetworking.registerGlobalReceiver(packet.getId(), packet::receive);
		return packet;
	}
}

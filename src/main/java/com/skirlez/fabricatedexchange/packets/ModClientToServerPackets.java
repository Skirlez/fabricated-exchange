package com.skirlez.fabricatedexchange.packets;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public abstract class ModClientToServerPackets {
	
	public static final ChargeItem CHARGE_ITEM
		= new ChargeItem("charge_item");
	public static final CycleItemMode CYCLE_ITEM_MODE
		= new CycleItemMode("cycle_item_mode");
	public static final FloorEmc FLOOR_EMC
		= new FloorEmc("floor_emc");
	public static final DoItemExtraFunction DO_ITEM_EXTRA_FUNCTION
		= new DoItemExtraFunction("do_item_extra_function");
	public static final UpdateTransmutationWidget UPDATE_TRANSMUTATION_WIDGET
		= new UpdateTransmutationWidget("update_transmutation_widget");

	 
	public static final ClientToServerPacket[] PACKETS = {
			CHARGE_ITEM, CYCLE_ITEM_MODE, FLOOR_EMC,
			DO_ITEM_EXTRA_FUNCTION, UPDATE_TRANSMUTATION_WIDGET};
	
	public static void register() {
		for (ClientToServerPacket packet : PACKETS) {
			registerPacket(packet);
		}
	}
	private static <T extends ClientToServerPacket> T registerPacket(T packet) {
		ServerPlayNetworking.registerGlobalReceiver(packet.getId(), packet::receive);
		return packet;
	}
}

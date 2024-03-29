package com.skirlez.fabricatedexchange.packets;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public abstract class ServerToClientPacket extends Identifiable {
	public ServerToClientPacket(String name) {
		super(name);
	}
	public abstract void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender);
	
}

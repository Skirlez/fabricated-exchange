package com.skirlez.fabricatedexchange.packets;

import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class UpdateTransmutationLastPage extends ServerToClientPacket {
	public UpdateTransmutationLastPage(String name) {
		super(name);
	}
	public void send(ServerPlayerEntity player, int lastPage) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeInt(lastPage);
		ServerPlayNetworking.send(player, id, buf);
	}
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		if (client.player.currentScreenHandler instanceof TransmutationTableScreenHandler screenHandler)
			screenHandler.setLastPageNum(buf.readInt());
	}
}




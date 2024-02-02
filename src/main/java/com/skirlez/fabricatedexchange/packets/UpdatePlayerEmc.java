package com.skirlez.fabricatedexchange.packets;

import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.util.SuperNumber;
public class UpdatePlayerEmc extends ServerToClientPacket {
	public UpdatePlayerEmc(String name) {
		super(name);
	}
	public void send(ServerPlayerEntity player, SuperNumber emc) {
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeString(emc.divisionString());
		ServerPlayNetworking.send(player, id, buffer);	
	}
	
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		FabricatedExchangeClient.clientEmc = new SuperNumber(buf.readString());
	}
}

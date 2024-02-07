package com.skirlez.fabricatedexchange.packets;

import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class FloorEmc extends ClientToServerPacket {
	public FloorEmc(String name) {
		super(name);
	}

	public void send() {
		ClientPlayNetworking.send(id, PacketByteBufs.create());
	}

	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
			PacketByteBuf buf, PacketSender responseSender) {   
		if (!(player.currentScreenHandler instanceof TransmutationTableScreenHandler))
			return;
		PlayerState state = ServerState.getPlayerState(player);
		if (state.emc.isRound()) 
			return;
			
		state.emc.floor();
		state.markDirty();
		ModServerToClientPackets.UPDATE_PLAYER_EMC.send(player, state.emc);
	}
}
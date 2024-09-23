package com.skirlez.fabricatedexchange.packets;

import com.skirlez.fabricatedexchange.block.ConsumerBlockEntity;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class UpdateConsumerBlock extends ServerToClientPacket {
	public UpdateConsumerBlock(String name) {
		super(name);
	}

	public void send(ServerPlayerEntity player, BlockPos pos, long emc) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeBlockPos(pos);
		buf.writeString(Long.toString(emc));
		ServerPlayNetworking.send(player, id, buf);
	}
	
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		BlockPos pos = buf.readBlockPos();
		long emc = Long.parseLong(buf.readString());
		if (client.world.getBlockEntity(pos) instanceof ConsumerBlockEntity blockEntity)
			blockEntity.update(emc);
	}


}

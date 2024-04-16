package com.skirlez.fabricatedexchange.packets;

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.skirlez.fabricatedexchange.BlockTransmutation;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

public class UpdateBlockTransmutationMap extends ServerToClientPacket {
	public UpdateBlockTransmutationMap(String name) {
		super(name);
	}

	public void send(ServerPlayerEntity player) {
		PacketByteBuf buf = PacketByteBufs.create();
		Set<Block> keySet = BlockTransmutation.blockTransmutationMap.keySet();
		buf.writeInt(keySet.size());
		Iterator<Block> iterator = keySet.iterator();
		while (iterator.hasNext()) {
			Block block = iterator.next();
			buf.writeIdentifier(Registry.BLOCK.getId(block));
			buf.writeIdentifier(Registry.BLOCK.getId(BlockTransmutation.blockTransmutationMap.get(block)));
		}
		ServerPlayNetworking.send(player, id, buf);
	}
	
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		int iterations = buf.readInt();
		ImmutableMap.Builder<Block, Block> builder = ImmutableMap.builder();
		for (int i = 0; i < iterations; i++) {
			Block block1 = Registry.BLOCK.get(buf.readIdentifier());
			Block block2 = Registry.BLOCK.get(buf.readIdentifier());
			if (block1 == null || block2 == null)
				continue;
			builder.put(block1, block2);
		}
		BlockTransmutation.blockTransmutationMap = builder.build();
	}
	
}

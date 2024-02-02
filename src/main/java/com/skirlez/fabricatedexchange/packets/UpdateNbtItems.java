package com.skirlez.fabricatedexchange.packets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UpdateNbtItems extends ServerToClientPacket {

	public UpdateNbtItems(String name) {
		super(name);
	}

	public void send(ServerPlayerEntity player) {
		PacketByteBuf buf = PacketByteBufs.create();
		Map<String, List<String>> nbtItems = ModDataFiles.NBT_ITEMS.getValue();
		
		int totalItems = nbtItems.size();
		buf.writeInt(totalItems);
		for (Map.Entry<String, List<String>> entry : nbtItems.entrySet()) {
			Identifier id = new Identifier(entry.getKey());
			List<String> nbtKeys = entry.getValue();
			int size = nbtKeys.size();
			
			buf.writeIdentifier(id);
			buf.writeInt(size);
			for (String nbtKey : nbtKeys) {
				buf.writeString(nbtKey);
			}
		}
		ServerPlayNetworking.send(player, id, buf);
	}
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf,
			PacketSender responseSender) {
		
		int totalItems = buf.readInt();
		HashMap<String, List<String>> nbtItems = new HashMap<String, List<String>>(totalItems);
		for (int i = 0; i < totalItems; i++) {
			String id = buf.readIdentifier().toString();
			
			int size = buf.readInt();
			List<String> nbtKeys = new ArrayList<String>(size);
			for (int j = 0; j < size; j++)
				nbtKeys.add(buf.readString());
			nbtItems.put(id, nbtKeys);
		}
		ModDataFiles.NBT_ITEMS.setValue(nbtItems);
		
	}
}

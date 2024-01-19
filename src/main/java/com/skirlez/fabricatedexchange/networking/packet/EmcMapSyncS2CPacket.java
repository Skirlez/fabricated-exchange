package com.skirlez.fabricatedexchange.networking.packet;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class EmcMapSyncS2CPacket {
	public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		EmcData.emcMap.clear();
		EmcData.potionEmcMap.clear();
		EmcData.enchantmentEmcMap.clear();
		int iterations = buf.readInt();
		for (int i = 0; i < iterations; i++) {
			Identifier id = buf.readIdentifier();
			EmcData.emcMap.put(Registries.ITEM.get(id), new SuperNumber(buf.readString()));
		}
		int potionIterations = buf.readInt();
		for (int i = 0; i < potionIterations; i++) {
			Identifier id = buf.readIdentifier();
			EmcData.potionEmcMap.put(Registries.POTION.get(id), new SuperNumber(buf.readString()));
		}
		int enchantmentIterations = buf.readInt();
		for (int i = 0; i < enchantmentIterations; i++) {
			Identifier id = buf.readIdentifier();
			EmcData.enchantmentEmcMap.put(Registries.ENCHANTMENT.get(id), new SuperNumber(buf.readString()));
		}
	}
}




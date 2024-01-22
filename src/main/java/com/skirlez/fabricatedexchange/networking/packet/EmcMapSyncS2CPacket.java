package com.skirlez.fabricatedexchange.networking.packet;

import java.util.HashMap;
import java.util.Map;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class EmcMapSyncS2CPacket {
	public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		Map<Item, SuperNumber> emcMap = new HashMap<Item, SuperNumber>();
		Map<Potion, SuperNumber> potionEmcMap = new HashMap<Potion, SuperNumber>();
		Map<Enchantment, SuperNumber> enchantmentEmcMap = new HashMap<Enchantment, SuperNumber>();
		int iterations = buf.readInt();
		for (int i = 0; i < iterations; i++) {
			Identifier id = buf.readIdentifier();
			emcMap.put(Registries.ITEM.get(id), new SuperNumber(buf.readString()));
		}
		int potionIterations = buf.readInt();
		for (int i = 0; i < potionIterations; i++) {
			Identifier id = buf.readIdentifier();
			potionEmcMap.put(Registries.POTION.get(id), new SuperNumber(buf.readString()));
		}
		int enchantmentIterations = buf.readInt();
		for (int i = 0; i < enchantmentIterations; i++) {
			Identifier id = buf.readIdentifier();
			enchantmentEmcMap.put(Registries.ENCHANTMENT.get(id), new SuperNumber(buf.readString()));
		}
		EmcData.emcMap = emcMap;
		EmcData.potionEmcMap = potionEmcMap;
		EmcData.enchantmentEmcMap = enchantmentEmcMap;
	}
}




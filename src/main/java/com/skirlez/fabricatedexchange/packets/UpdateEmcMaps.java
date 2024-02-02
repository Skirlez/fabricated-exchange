package com.skirlez.fabricatedexchange.packets;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UpdateEmcMaps extends ServerToClientPacket {
	public UpdateEmcMaps(String name) {
		super(name);
	}

	public void send(ServerPlayerEntity player) {
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeInt(EmcData.emcMap.keySet().size());
		for (Item s : EmcData.emcMap.keySet()) {
			buffer.writeIdentifier(Registries.ITEM.getId(s));
			buffer.writeString(EmcData.emcMap.get(s).divisionString());
		}
		buffer.writeInt(EmcData.potionEmcMap.keySet().size());
		for (Potion s : EmcData.potionEmcMap.keySet()) {
			buffer.writeIdentifier(Registries.POTION.getId(s));
			buffer.writeString(EmcData.potionEmcMap.get(s).divisionString());
		}
		buffer.writeInt(EmcData.enchantmentEmcMap.keySet().size());
		for (Enchantment s : EmcData.enchantmentEmcMap.keySet()) {
			buffer.writeIdentifier(Registries.ENCHANTMENT.getId(s));
			buffer.writeString(EmcData.enchantmentEmcMap.get(s).divisionString());
		}

		ServerPlayNetworking.send(player, id, buffer);	
	}
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
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
		EmcData.emcMap = ImmutableMap.copyOf(emcMap);
		EmcData.potionEmcMap = ImmutableMap.copyOf(potionEmcMap);
		EmcData.enchantmentEmcMap = ImmutableMap.copyOf(enchantmentEmcMap);
	}
}




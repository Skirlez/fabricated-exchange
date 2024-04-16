package com.skirlez.fabricatedexchange.packets;

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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class UpdateEmcMaps extends ServerToClientPacket {
	public UpdateEmcMaps(String name) {
		super(name);
	}

	public void send(ServerPlayerEntity player) {
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeInt(EmcData.emcMap.keySet().size());
		for (Item s : EmcData.emcMap.keySet()) {
			buffer.writeIdentifier(Registry.ITEM.getId(s));
			buffer.writeString(EmcData.emcMap.get(s).divisionString());
		}
		buffer.writeInt(EmcData.potionEmcMap.keySet().size());
		for (Potion s : EmcData.potionEmcMap.keySet()) {
			buffer.writeIdentifier(Registry.POTION.getId(s));
			buffer.writeString(EmcData.potionEmcMap.get(s).divisionString());
		}
		buffer.writeInt(EmcData.enchantmentEmcMap.keySet().size());
		for (Enchantment s : EmcData.enchantmentEmcMap.keySet()) {
			buffer.writeIdentifier(Registry.ENCHANTMENT.getId(s));
			buffer.writeString(EmcData.enchantmentEmcMap.get(s).divisionString());
		}

		ServerPlayNetworking.send(player, id, buffer);	
	}
	public void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		ImmutableMap.Builder<Item, SuperNumber> emcMapBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<Potion, SuperNumber> potionEmcMapBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<Enchantment, SuperNumber> enchantmentEmcMapBuilder = ImmutableMap.builder();
		int iterations = buf.readInt();
		for (int i = 0; i < iterations; i++) {
			Identifier id = buf.readIdentifier();
			emcMapBuilder.put(Registry.ITEM.get(id), new SuperNumber(buf.readString()));
		}
		int potionIterations = buf.readInt();
		for (int i = 0; i < potionIterations; i++) {
			Identifier id = buf.readIdentifier();
			potionEmcMapBuilder.put(Registry.POTION.get(id), new SuperNumber(buf.readString()));
		}
		int enchantmentIterations = buf.readInt();
		for (int i = 0; i < enchantmentIterations; i++) {
			Identifier id = buf.readIdentifier();
			enchantmentEmcMapBuilder.put(Registry.ENCHANTMENT.get(id), new SuperNumber(buf.readString()));
		}
		EmcData.emcMap = emcMapBuilder.build();
		EmcData.potionEmcMap = potionEmcMapBuilder.build();
		EmcData.enchantmentEmcMap = enchantmentEmcMapBuilder.build();
	}
}




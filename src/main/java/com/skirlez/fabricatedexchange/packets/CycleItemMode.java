package com.skirlez.fabricatedexchange.packets;
import com.skirlez.fabricatedexchange.item.ItemWithModes;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class CycleItemMode extends ClientToServerPacket {
	public CycleItemMode(String name) {
		super(name);
	}
	public void send() {
		ClientPlayNetworking.send(id, PacketByteBufs.create());
	}
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
							   PacketByteBuf buf, PacketSender responseSender) {
		server.execute(new Runnable() {
			@Override
			public void run() {
				ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
				if (!(stack.getItem() instanceof ItemWithModes)) {
					return;
				}
				ItemWithModes.cycleModes(stack, player);
			}
		});
	}
}

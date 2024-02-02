package com.skirlez.fabricatedexchange.packets;

import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class DoItemExtraFunction extends ClientToServerPacket {
	public DoItemExtraFunction(String name) {
		super(name);
	}
	public void send() {
		ClientPlayNetworking.send(id, PacketByteBufs.create());
	}
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
			PacketByteBuf buf, PacketSender responseSender) {   
		ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
		if (!(stack.getItem() instanceof ExtraFunctionItem)) {
			return;
		}

		final ItemStack theStack = stack;
		ExtraFunctionItem item = (ExtraFunctionItem)(stack.getItem());
		server.execute(new Runnable() {
			@Override
			public void run() {
				if (player.isDisconnected())
					return;
				item.doExtraFunction(theStack, player);
			}
		});
	}


}



package com.skirlez.fabricatedexchange.packets;

import com.skirlez.fabricatedexchange.item.ChargeableItem;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class ChargeItem extends ClientToServerPacket {
	public ChargeItem(String name) {
		super(name);
	}

	public void send() {
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeBoolean(Screen.hasShiftDown());
		ClientPlayNetworking.send(id, buffer);
	}
	
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
			PacketByteBuf buf, PacketSender responseSender) {   
		ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
		if (!(stack.getItem() instanceof ChargeableItem)) {
			stack = player.getStackInHand(Hand.OFF_HAND);
			if (!(stack.getItem() instanceof ChargeableItem))
				return;
		}
		ChargeableItem item = (ChargeableItem)(stack.getItem());
		boolean hasShiftDown = buf.readBoolean();
		int value = (hasShiftDown) ? -1 : 1;
		ChargeableItem.chargeStack(stack, value, 0, item.getMaxCharge(), player);   
	}
	
	public void register() {
		
		
	}
}



package com.skirlez.fabricatedexchange.networking.packet;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class ItemChargeC2SPacket {
	public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
			PacketByteBuf buf, PacketSender responseSender) {   

		boolean hasShiftDown = buf.readBoolean();
		ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
		if (!(stack.getItem() instanceof ChargeableItem)) {
			stack = player.getStackInHand(Hand.OFF_HAND);
			if (!(stack.getItem() instanceof ChargeableItem))
				return;
		}
		ChargeableItem item = (ChargeableItem)(stack.getItem());

		int value = (hasShiftDown) ? -1 : 1;
		ChargeableItem.chargeStack(stack, value, 0, item.getMaxCharge(), player);   
	}

}



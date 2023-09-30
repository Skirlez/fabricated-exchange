package com.skirlez.fabricatedexchange.networking.packet;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public class CycleItemModeC2SPacket {
	public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
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

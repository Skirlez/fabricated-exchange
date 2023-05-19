package com.skirlez.fabricatedexchange.networking.packet;

import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import com.skirlez.fabricatedexchange.util.IPlayerDataSaver;

public class EMCSyncS2CPacket {
   public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
      ((IPlayerDataSaver) client.player).getPersistentData().putString("emc", buf.readString());
   }
}

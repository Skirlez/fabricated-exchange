package com.skirlez.fabricatedexchange.networking.packet;

import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class TransmutationTableMaxPageS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        if (client.player.currentScreenHandler instanceof TransmutationTableScreenHandler screenHandler) {
            screenHandler.setLastPageNum(buf.readInt());
        }
    }
}




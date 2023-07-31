package com.skirlez.fabricatedexchange.networking.packet;

import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class TransmutationTableWidgetsC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {   
        if (player.currentScreenHandler instanceof TransmutationTableScreenHandler screenHandler) {
            int type = buf.readInt();
            if (type == 0) {
                screenHandler.setSearchText(buf.readString());
                screenHandler.refreshOffering();
            }
            else
                screenHandler.changeOfferingPage((type == 1) ? -1 : 1); // type 1 is left, type 2 is right
        }
      
    }
}



package com.skirlez.fabricatedexchange.networking.packet;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandler;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class FloorEmcC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {   
        if (!(player.currentScreenHandler instanceof TransmutationTableScreenHandler))
            return;
        PlayerState state = ServerState.getPlayerState(player);
        if (state.emc.isWhole()) 
            return;
            
        state.emc.floor();
        state.markDirty();
        EmcData.syncEmc(player, state.emc);
    }
}

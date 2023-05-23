package com.skirlez.fabricatedexchange.util;

import java.math.BigInteger;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.networking.ModMessages;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class EmcData {
    public static SuperNumber getEmc(LivingEntity player) {
        PlayerState playerState = ServerState.getPlayerState(player);
        return playerState.emc;
    } 

    public static void setEmc(LivingEntity player, SuperNumber amount) {
        PlayerState playerState = ServerState.getPlayerState(player);
        playerState.emc = amount;
        playerState.markDirty();
        syncEmc((ServerPlayerEntity) player, playerState.emc);
    }    

    public static void addEmc(LivingEntity player, SuperNumber amount) {
        PlayerState playerState = ServerState.getPlayerState(player);
        playerState.emc.add(amount);
        playerState.markDirty();
        syncEmc((ServerPlayerEntity) player, playerState.emc);
    }    
    public static void subtractEmc(LivingEntity player, SuperNumber amount) {
        PlayerState playerState = ServerState.getPlayerState(player);
        playerState.emc.subtract(amount);
        playerState.markDirty();
        syncEmc((ServerPlayerEntity) player, playerState.emc);
    }    

    public static void syncEmc(ServerPlayerEntity player, SuperNumber emc) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeString(emc.divisonString());
        ServerPlayNetworking.send(player, ModMessages.EMC_SYNC_IDENTIFIER, buffer);
    }
}

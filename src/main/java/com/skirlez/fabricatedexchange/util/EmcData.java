package com.skirlez.fabricatedexchange.util;

import java.math.BigInteger;

import com.skirlez.fabricatedexchange.networking.ModMessages;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class EmcData {
    public static BigInteger getEmc(LivingEntity player) {
        PlayerState playerState = ServerState.getPlayerState(player);
        return playerState.emc;
    } 

    public static void setEmc(LivingEntity player, BigInteger amount) {
        PlayerState playerState = ServerState.getPlayerState(player);
        playerState.emc = amount;
        playerState.markDirty();
        syncEmc((ServerPlayerEntity) player, playerState.emc);
    }    

    public static void addEmc(LivingEntity player, BigInteger amount) {
        PlayerState playerState = ServerState.getPlayerState(player);
        playerState.emc.add(amount);
        playerState.markDirty();
        syncEmc((ServerPlayerEntity) player, playerState.emc);
    }    


    public static void syncEmc(ServerPlayerEntity player, BigInteger emc) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeString(emc.toString());
        ServerPlayNetworking.send(player, ModMessages.EMC_SYNC_IDENTIFIER, buffer);
    }
}

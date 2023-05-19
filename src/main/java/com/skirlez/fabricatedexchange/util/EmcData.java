package com.skirlez.fabricatedexchange.util;

import java.math.BigInteger;

import com.skirlez.fabricatedexchange.networking.ModMessages;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class EmcData {
    public static BigInteger getEmc(IPlayerDataSaver player) {
        NbtCompound nbt = player.getPersistentData();
        return new BigInteger(nbt.getString("emc"));
    } 

    public static void setEmc(IPlayerDataSaver player, BigInteger amount) {
        NbtCompound nbt = player.getPersistentData();
        nbt.putString("emc", amount.toString());
        syncEmc(amount, (ServerPlayerEntity) player); 
    }    

    public static void addEmc(IPlayerDataSaver player, BigInteger amount) {
        NbtCompound nbt = player.getPersistentData();
        BigInteger emc = new BigInteger(nbt.getString("emc"));
        emc.add(amount);
        nbt.putString("emc", emc.toString());
        syncEmc(emc, (ServerPlayerEntity) player);
    }    


    public static void syncEmc(BigInteger emc, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeString(emc.toString());
        ServerPlayNetworking.send(player, ModMessages.EMC_SYNC_IDENTIFIER, buffer);
    }
}

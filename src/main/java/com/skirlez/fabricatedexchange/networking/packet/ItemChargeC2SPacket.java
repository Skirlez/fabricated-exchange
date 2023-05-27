package com.skirlez.fabricatedexchange.networking.packet;


import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.PhilosophersStone;
import com.skirlez.fabricatedexchange.sound.ModSounds;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;

public class ItemChargeC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
            PacketByteBuf buf, PacketSender responseSender) {   

        boolean hasShiftDown = buf.readBoolean();
        ItemStack mainStack = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offStack = player.getStackInHand(Hand.OFF_HAND);
        int value;
        value = (hasShiftDown) ? -1 : 1;
        if (mainStack.getItem().equals(ModItems.PHILOSOPHERS_STONE)) {
            chargeStack(mainStack, PhilosophersStone.CHARGE_KEY, value, 
                PhilosophersStone.maxCharge, 0, player);
           
        }
        else if (offStack.getItem().equals(ModItems.PHILOSOPHERS_STONE)) { 
            chargeStack(offStack, PhilosophersStone.CHARGE_KEY, value, 
                PhilosophersStone.maxCharge, 0, player);
        
        }
        
    }

    private static void chargeStack(ItemStack stack, String key, int value, int max, int min, ServerPlayerEntity player) {
        NbtCompound stackNbt = stack.getOrCreateNbt();
        int oldValue = stackNbt.getInt(key);
        int newValue = oldValue + value;
        if (newValue > max)
            newValue = max;
        else if (newValue < min)
            newValue = min;
        if (oldValue != newValue) {
            stackNbt.putInt(key, newValue);
            SoundEvent sound = (Math.signum(value) == 1) ? ModSounds.ITEM_CHARGE : ModSounds.ITEM_DISCHARGE;
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), sound, 
                    SoundCategory.PLAYERS, 1, 0.5F + ((0.5F / (float) max) * oldValue));
        }

    }
}



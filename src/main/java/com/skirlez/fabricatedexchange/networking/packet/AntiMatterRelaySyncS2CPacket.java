package com.skirlez.fabricatedexchange.networking.packet;

import com.skirlez.fabricatedexchange.block.AntiMatterRelayBlockEntity;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class AntiMatterRelaySyncS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        SuperNumber emc = new SuperNumber(buf.readString());
        BlockPos pos = buf.readBlockPos();
    
        if (client.world.getBlockEntity(pos) instanceof AntiMatterRelayBlockEntity blockEntity) {
            blockEntity.update(emc);
        }
    }
}

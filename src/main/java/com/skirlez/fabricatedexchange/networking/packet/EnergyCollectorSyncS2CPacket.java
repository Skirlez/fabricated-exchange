package com.skirlez.fabricatedexchange.networking.packet;

import com.skirlez.fabricatedexchange.block.EnergyCollectorBlockEntity;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class EnergyCollectorSyncS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        BlockPos pos = buf.readBlockPos();
        SuperNumber emc = new SuperNumber(buf.readString());
 
        if (client.world.getBlockEntity(pos) instanceof EnergyCollectorBlockEntity blockEntity)
            blockEntity.update(emc);
        

    }
}

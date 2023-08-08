package com.skirlez.fabricatedexchange.networking.packet;

import java.util.HashMap;
import java.util.Map;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BlockTransmutationSyncS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        int iterations = buf.readInt();
        Map<Block, Block> newBlockTransmutationMap = new HashMap<Block, Block>();
        for (int i = 0; i < iterations; i++) {
            String block1 = buf.readString();
            String block2 = buf.readString();
            newBlockTransmutationMap.put(Registries.BLOCK.get(new Identifier(block1)), Registries.BLOCK.get(new Identifier(block2)));
        }
        FabricatedExchange.blockTransmutationMap = newBlockTransmutationMap;
    }
}

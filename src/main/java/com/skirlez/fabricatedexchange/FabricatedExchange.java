package com.skirlez.fabricatedexchange;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skirlez.fabricatedexchange.block.ModBlockEntities;
import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.emc.EmcMapper;
import com.skirlez.fabricatedexchange.item.ModItemGroups;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.ModConfig;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;

public class FabricatedExchange implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabricated-exchange");
    public static final String MOD_ID = "fabricated-exchange";
    // this map tells the philosopher's stone what block to transform when right clicked
    public static Map<Block, Block> blockTransmutationMap = new HashMap<>();
    

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModSounds.registerSoundEvents();
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerAllScreenHandlers();
        ModMessages.registerC2SPackets();
        fetchBlockRotationMap();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> { 
            // send the player's own emc value
            PlayerState playerState = ServerState.getPlayerState(handler.player);
            EmcData.syncEmc((ServerPlayerEntity)handler.player, playerState.emc);

            // send the entire emc map
            PacketByteBuf buffer = PacketByteBufs.create();
            int iterations = EmcData.emcMap.keySet().size();
            buffer.writeInt(iterations);
            Iterator<String> iterator = EmcData.emcMap.keySet().iterator();
            for (int i = 0; i < iterations; i++) {
                String s = (String)iterator.next();
                buffer.writeString(s);
                buffer.writeString(EmcData.emcMap.get(s).divisionString());
            }
            ServerPlayNetworking.send(handler.player, ModMessages.EMC_MAP_SYNC_IDENTIFIER, buffer);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            EmcMapper mapper = new EmcMapper(EmcData.emcMap);
            mapper.fillEmcMap(server.getOverworld(), server.getOverworld().getRecipeManager());
            
            HashMap<String, SuperNumber> customEmcMap = ModConfig.CUSTOM_EMC_MAP_FILE.fetchAndGetValue();
            if (customEmcMap != null)
                mapper.mergeMap(new HashMap<String, SuperNumber>(customEmcMap));
        });
    }


    private void fetchBlockRotationMap() {
        blockTransmutationMap.clear();
        String[][] blockTransmutationData = ModConfig.BLOCK_TRANSMUTATION_MAP_FILE.fetchAndGetValue();
        if (blockTransmutationData == null)
            return;
        for (int i = 0; i < blockTransmutationData.length; i++) {
            int j = 0;
            // GSON just packages the string[][] with a FREE (100% off) NULL™ for no reason so i guess we have to check for it
            if (blockTransmutationData[i] == null) 
                continue;
            int len = blockTransmutationData[i].length;
            if (len == 0)
                continue;
            if (len == 3 && blockTransmutationData[i][j].equals("O")) {
                addBlockRelation(blockTransmutationData[i][j + 1], blockTransmutationData[i][j + 2]); 
                continue;
            }
            while (j < len - 1) {
                addBlockRelation(blockTransmutationData[i][j], blockTransmutationData[i][j + 1]); 
                j++;
            }
            addBlockRelation(blockTransmutationData[i][j], blockTransmutationData[i][0]); 
        }
    }

    private void addBlockRelation(String str1, String str2) {
        Block b1 = Registries.BLOCK.get(new Identifier(str1));
        Block b2 = Registries.BLOCK.get(new Identifier(str2));
        if (b1 == null || b2 == null) {
            FabricatedExchange.LOGGER.error("Invalid block(s) found in block_transmutation_map.json! Block 1: " + str1 + " -> Block 2: " + str2);
            return;
        }
        if (blockTransmutationMap.containsKey(b1)) {
            FabricatedExchange.LOGGER.error("Duplicate block transmutation in block_transmutation_map.json! Block 1: " + str1 + " -> Block 2: " + str2);
            return;
        }
        blockTransmutationMap.put(b1, b2);
    };
     


}
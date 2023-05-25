package com.skirlez.fabricatedexchange;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skirlez.fabricatedexchange.block.ModBlockEntities;
import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.emc.EmcMapper;
import com.skirlez.fabricatedexchange.item.ModItemGroups;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.EmcData;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;

public class FabricatedExchange implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabricated-exchange");
    public static final String MOD_ID = "fabricated-exchange";
    // this map tells the philosopher's stone what block to transform when right clicked
    public static Map<Block, Block> blockRotationMap = new HashMap<>();
    

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModSounds.registerSoundEvents();
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerAllScreenHandlers();
        
        ModMessages.registerC2SPackets();
        fillBlockRotationMap();

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
                buffer.writeString(EmcData.emcMap.get(s).divisonString());
            }
            ServerPlayNetworking.send(handler.player, ModMessages.EMC_MAP_SYNC_IDENTIFIER, buffer);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            EmcMapper mapper = new EmcMapper(EmcData.emcMap);
            mapper.fillEmcMap(server.getOverworld(), server.getOverworld().getRecipeManager());

        });
   
    }



    private void fillBlockRotationMap() {
        addTwoWayRelation(Blocks.GRASS_BLOCK, Blocks.SAND);
        addOneWayRelation(Blocks.DIRT, Blocks.SAND);
        addTwoWayRelation(Blocks.GRASS, Blocks.DEAD_BUSH);
        addChainRelation(new Block[] {Blocks.OAK_WOOD, Blocks.BIRCH_WOOD, Blocks.SPRUCE_WOOD});
        addChainRelation(new Block[] {Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.SPRUCE_LOG}); 
    }

    private void addTwoWayRelation(Block b1, Block b2) {
        blockRotationMap.put(b1, b2);
        blockRotationMap.put(b2, b1);
    };
    private void addOneWayRelation(Block b1, Block b2) {
        blockRotationMap.put(b1, b2);
    };
    private void addChainRelation(Block[] arr) {
        int i;
        for (i = 0; i < arr.length - 1; i++) {
            blockRotationMap.put(arr[i], arr[i + 1]);
        }
        blockRotationMap.put(arr[i], arr[0]);
    };



}
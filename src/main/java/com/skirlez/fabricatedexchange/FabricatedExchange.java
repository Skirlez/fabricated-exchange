package com.skirlez.fabricatedexchange;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.skirlez.fabricatedexchange.block.ModBlockEntities;
import com.skirlez.fabricatedexchange.block.ModBlocks;
import com.skirlez.fabricatedexchange.command.TheCommand;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.emc.EmcMapper;
import com.skirlez.fabricatedexchange.item.ModItemGroups;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.screen.ModScreenHandlers;
import com.skirlez.fabricatedexchange.sound.ModSounds;
import com.skirlez.fabricatedexchange.util.ModTags;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModConfig;


public class FabricatedExchange implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fabricated-exchange");
    public static final String MOD_ID = "fabricated-exchange";
    // this map tells the philosopher's stone what block to transform when right clicked
    public static Map<Block, Block> blockTransmutationMap = new HashMap<Block, Block>();
    // this map will be filled with all the items that have the transmutation_fuel tag, and each key value pair will represent a jump in progression between the fuel items
    // (e.g. in vanilla, coal (32 emc) will have a key value pair with redstone (64 emc), the next item with the fuel tag that has more emc than it.)
    public static Map<Item, Item> fuelProgressionMap = new HashMap<Item, Item>();

    
    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModSounds.registerSoundEvents();
        ModBlocks.registerModBlocks();
        ModBlockEntities.registerBlockEntities();
        ModScreenHandlers.registerAllScreenHandlers();
        ModMessages.registerC2SPackets();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TheCommand.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> { 
            // send the player's own emc value
            ServerPlayerEntity player = handler.player;
            PlayerState playerState = ServerState.getPlayerState(player);
            EmcData.syncEmc(player, playerState.emc);
            EmcData.syncMap(player);
            syncBlockTransmutation(player);
        });

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            ModConfig.fetchAll();
            generateBlockRotationMap(ModConfig.BLOCK_TRANSMUTATION_MAP_FILE.getValue());
            reloadEmcMap(server);
        });
    }

    public static boolean reloadEmcMap(MinecraftServer server) {
        
        //EmcMapper mapper = new EmcMapper();

        //mapper.fillEmcMap(server.getOverworld(), server.getOverworld().getRecipeManager());
        
        EmcMapper mapper = new EmcMapper(server.getRegistryManager(), server.getOverworld().getRecipeManager());
        boolean hasWarned = mapper.map();
        EmcData.emcMap = mapper.getEmcMap();
        EmcData.potionEmcMap =  mapper.getPotionEmcMap();
        List<Item> fuelItemList = new ArrayList<Item>();
 
        Iterator<RegistryEntry<Item>> iterator = Registries.ITEM.getEntryList(ModTags.FUEL).get().iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next().value();

            int index = Collections.binarySearch(fuelItemList, item, 
                (item1, item2) -> 
                    EmcData.getItemEmc(item1)
                    .compareTo(
                    EmcData.getItemEmc(item2)));

            if (index < 0)
                index = -index - 1;

            fuelItemList.add(index, item);
            //GeneralUtil.addSortedEmcList(fuelItemList, item, false);
        }
        Map<Item, Item> newFuelProgressionMap = new HashMap<Item, Item>();
        for (int i = 0; i < fuelItemList.size(); i++) {
            Item item = fuelItemList.get(i);
            SuperNumber itemEmc = EmcData.getItemEmc(item);
            for (int j = i; j < fuelItemList.size(); j++) {
                Item nextItem = fuelItemList.get(j);
                SuperNumber nextEmc = EmcData.getItemEmc(nextItem);
                if (itemEmc.compareTo(nextEmc) == -1) { // if this item's emc is smaller than the next one
                    newFuelProgressionMap.put(item, nextItem);
                    break;
                }
            }
        }
        fuelProgressionMap = newFuelProgressionMap;


        return hasWarned;
    }

    public static void syncMaps(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            EmcData.syncMap(player);
        }
    }
    public static void syncBlockTransmutations(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            syncBlockTransmutation(player);
        }
    }

    private static void syncBlockTransmutation(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        Set<Block> keySet = blockTransmutationMap.keySet();
        buf.writeInt(keySet.size());
        Iterator<Block> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            buf.writeString(Registries.BLOCK.getId(block).toString());
            buf.writeString(Registries.BLOCK.getId(blockTransmutationMap.get(block)).toString());
        }
        ServerPlayNetworking.send(player, ModMessages.BLOCK_TRANSMUTATION_SYNC_IDENTIFIER, buf);
    }

    public static void generateBlockRotationMap(String[][] blockTransmutationData) {
        if (blockTransmutationData == null)
            return;
        blockTransmutationMap.clear();
        for (int i = 0; i < blockTransmutationData.length; i++) {
            int j = 0;
            // GSON just packages the String[][] with a FREE (100% off) NULL™ for no reason so i guess we have to check for it
            if (blockTransmutationData[i] == null) 
                continue;
            int len = blockTransmutationData[i].length;
            if (len == 0)
                continue;
            if (len == 1) {
                String str = blockTransmutationData[i][j];
                String[] parts = str.split("#");
                addBlockRelation(parts[0], parts[1]); 
                continue;
            }
            while (j < len - 1) {
                addBlockRelation(blockTransmutationData[i][j], blockTransmutationData[i][j + 1]); 
                j++;
            }
            addBlockRelation(blockTransmutationData[i][j], blockTransmutationData[i][0]); 
        }
    }

    private static void addBlockRelation(String str1, String str2) {
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
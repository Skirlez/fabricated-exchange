package com.skirlez.fabricatedexchange.emc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.util.DataFile;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.ModConfig;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

public class EmcData {

    // both the server and the client can use these
    public static ConcurrentMap<String, SuperNumber> emcMap = new ConcurrentHashMap<String, SuperNumber>();

    // these should only ever be equal to what's in the seed emc json
    public static Map<String, SuperNumber> seedEmcMap = new HashMap<String, SuperNumber>();
    public static Map<String, SuperNumber> customEmcMap = new HashMap<String, SuperNumber>();

    public static SuperNumber getItemEmc(Item item) {
        if (item == null)
            return SuperNumber.Zero(); 
        String id = Registries.ITEM.getId(item).toString();
        return getItemEmc(id);
    }
    public static SuperNumber getItemStackEmc(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item == null)
            return SuperNumber.Zero(); 
        String id = Registries.ITEM.getId(item).toString();
        SuperNumber baseEmc = getItemEmc(id);
        baseEmc.multiply(itemStack.getCount());
        return baseEmc;
    }
    public static SuperNumber getItemEmc(String id) {
        if (emcMap.containsKey(id)) {
            return new SuperNumber(emcMap.get(id));
        }
        return SuperNumber.Zero(); 
    }
    public static boolean isItemInSeedValues(Item item) {
        return seedEmcMap.containsKey(Registries.ITEM.getId(item).toString());
    }
    public static boolean isItemInCustomValues(Item item) {
        return customEmcMap.containsKey(Registries.ITEM.getId(item).toString());
    }
    // only the server can use these
    public static SuperNumber getEmc(LivingEntity player) {
        PlayerState playerState = ServerState.getPlayerState(player);
        return playerState.emc;
    } 
    public static void setItemEmc(Item item, SuperNumber emc, boolean seed) {
        DataFile<Map<String, SuperNumber>> file = seed ? ModConfig.SEED_EMC_MAP_FILE : ModConfig.CUSTOM_EMC_MAP_FILE;
        if (item == null)
            return;
        String id = Registries.ITEM.getId(item).toString();
        Map<String, SuperNumber> newEmcMap = file.getValue();
        if (newEmcMap == null)
            newEmcMap = new HashMap<String, SuperNumber>();
        newEmcMap.put(id, emc);
        file.setValueAndSave(newEmcMap);
        GeneralUtil.mergeMap(emcMap, newEmcMap);
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
        buffer.writeString(emc.divisionString());
        ServerPlayNetworking.send(player, ModMessages.EMC_SYNC_IDENTIFIER, buffer);
    }
    public static void syncMap(ServerPlayerEntity player) {
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
        ServerPlayNetworking.send(player, ModMessages.EMC_MAP_SYNC_IDENTIFIER, buffer);
    }



}

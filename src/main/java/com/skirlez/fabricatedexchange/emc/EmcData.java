package com.skirlez.fabricatedexchange.emc;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.skirlez.fabricatedexchange.item.NbtItem;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.util.DataFile;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModConfig;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EmcData {

    // both the server and the client can use these
    public static Map<String, SuperNumber> emcMap = new ConcurrentHashMap<String, SuperNumber>();
    public static Map<String, SuperNumber> potionEmcMap = new ConcurrentHashMap<String, SuperNumber>();

    // these should only ever be equal to what's in their respective jsons
    public static Map<String, SuperNumber> seedEmcMap = new HashMap<String, SuperNumber>();
    public static Map<String, SuperNumber> customEmcMap = new HashMap<String, SuperNumber>();


    public static SuperNumber getItemEmc(NbtItem item) {
        SuperNumber emc = getItemEmc(item.asItem());
        NbtCompound nbt = item.getNbt();
        if (nbt != null)
            considerNbt(item.asItem(), nbt, emc);
        return emc;
    }

    public static SuperNumber getItemEmc(Item item) {
        if (item == null)
            return SuperNumber.Zero(); 
        String id = Registry.ITEM.getId(item).toString();
        return getItemEmc(id);
    }
    public static SuperNumber getItemStackEmc(ItemStack itemStack) {
        if (itemStack.isEmpty())
            return SuperNumber.Zero(); 
        Item item = itemStack.getItem();
        String id = Registry.ITEM.getId(item).toString();
        SuperNumber emc = getItemEmc(id);
        emc.multiply(itemStack.getCount());
        considerStackDurability(itemStack, emc);
        considerStackNbt(itemStack, emc);
        return emc;
    }
    public static SuperNumber getItemEmc(String id) {
        if (emcMap.containsKey(id))
            return new SuperNumber(emcMap.get(id));
        return SuperNumber.Zero(); 
    }
    public static void considerStackDurability(ItemStack stack, SuperNumber emc) {
        if (stack.getMaxDamage() != 0) {
            emc.multiply(new SuperNumber(stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()));
            emc.floor();
        }
    }
    public static void considerStackNbt(ItemStack stack, SuperNumber emc) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null)
            return;
        considerNbt(stack.getItem(), stack.getNbt(), emc);
    }

    private static void considerNbt(Item item, NbtCompound nbt, SuperNumber emc) {
        if (nbt.contains("emc"))
            emc.add(new SuperNumber(nbt.getString("emc")));

        if (!ModConfig.NBT_ITEMS.hasItem(Registry.ITEM.getId(item).toString()))
            return;

        if (item instanceof PotionItem) {
            String potion = nbt.getString("Potion");
            if (!potion.isEmpty() && potionEmcMap.containsKey(potion)) {

                SuperNumber addition = potionEmcMap.get(potion);
                if (!addition.equalsZero())
                    emc.add(addition);
            }   
        }
        else if (item == Items.ENCHANTED_BOOK) {
            NbtList enchantments = nbt.getList("StoredEnchantments", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < enchantments.size(); i++) {
                NbtCompound enchantmentCompound = enchantments.getCompound(i);
                String enchantment = enchantmentCompound.getString("id");
                
                int repairCost = nbt.getInt("RepairCost");
                // anvil uses = log2(repairCost)
                int anvilUses = 32 - Integer.numberOfLeadingZeros(repairCost);
                // (x/7-1)^2
                SuperNumber repairCostPenalty = new SuperNumber(anvilUses, 7);
                repairCostPenalty.subtract(BigInteger.ONE);
                repairCostPenalty.square();          

                // TODO: Give enchantments EMC value (For now, all of them are worth 32)
                SuperNumber enchantmentEmc = new SuperNumber(32);
                int level = enchantmentCompound.getInt("lvl");
                enchantmentEmc.multiply(level);
                enchantmentEmc.multiply(repairCostPenalty);
                enchantmentEmc.floor();
                emc.add(enchantmentEmc);
            }
        }
        else if (nbt.contains("BlockEntityTag")) { // shulker box
            NbtList list = nbt.getCompound("BlockEntityTag").getList("Items", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < list.size(); i++) {
                NbtCompound itemCompound = list.getCompound(i);
                String id = itemCompound.getString("id");
                ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier(id)));
                stack.setCount(itemCompound.getInt("Count"));
                
                NbtCompound extraData = itemCompound.getCompound("tag");
                if (!extraData.isEmpty())
                    stack.setNbt(extraData);

                SuperNumber itemEmc = EmcData.getItemStackEmc(stack);
                if (itemEmc.equalsZero()) {
                    emc.copyValueOf(SuperNumber.ZERO);
                    return;
                }
                emc.add(itemEmc);
            }
        }
                
        
        return; 
    }



    public static boolean isItemInSeedValues(Item item) {
        return seedEmcMap.containsKey(Registry.ITEM.getId(item).toString());
    }
    public static boolean isItemInCustomValues(Item item) {
        return customEmcMap.containsKey(Registry.ITEM.getId(item).toString());
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
        String id = Registry.ITEM.getId(item).toString();
        Map<String, SuperNumber> newEmcMap = file.getValue();
        if (newEmcMap == null)
            newEmcMap = new HashMap<String, SuperNumber>();
        newEmcMap.put(id, emc);
        file.setValueAndSave(newEmcMap);
        GeneralUtil.mergeMap(emcMap, newEmcMap);
    }

    public static void setEmc(ServerPlayerEntity player, SuperNumber amount) {
        PlayerState playerState = ServerState.getPlayerState(player);
        playerState.emc = amount;
        syncEmc(player, playerState.emc);
    }    
    public static void addEmc(ServerPlayerEntity player, SuperNumber amount) {
        PlayerState playerState = ServerState.getPlayerState(player);
        playerState.emc.add(amount);
        syncEmc(player, playerState.emc);
    }    
    public static void subtractEmc(ServerPlayerEntity player, SuperNumber amount) {
        PlayerState playerState = ServerState.getPlayerState(player);
        playerState.emc.subtract(amount);
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
        buffer.writeInt(EmcData.emcMap.keySet().size());
        for (String s : EmcData.emcMap.keySet()) {
            buffer.writeString(s);
            buffer.writeString(EmcData.emcMap.get(s).divisionString());
        }
        buffer.writeInt(EmcData.potionEmcMap.keySet().size());
        for (String s : EmcData.potionEmcMap.keySet()) {
            buffer.writeString(s);
            buffer.writeString(EmcData.potionEmcMap.get(s).divisionString());
        }

        ServerPlayNetworking.send(player, ModMessages.EMC_MAP_SYNC_IDENTIFIER, buffer);
    }
}

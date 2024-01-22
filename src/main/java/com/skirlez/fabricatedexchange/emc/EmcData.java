package com.skirlez.fabricatedexchange.emc;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import com.skirlez.fabricatedexchange.item.NbtItem;
import com.skirlez.fabricatedexchange.networking.ModMessages;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import com.skirlez.fabricatedexchange.util.config.lib.DataFile;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EmcData {

	// both the server and the client can use these
	public static volatile Map<Item, SuperNumber> emcMap = new HashMap<Item, SuperNumber>();
	
	public static Map<Potion, SuperNumber> potionEmcMap = new HashMap<Potion, SuperNumber>();
	public static Map<Enchantment, SuperNumber> enchantmentEmcMap = new HashMap<Enchantment, SuperNumber>();

	// these should only ever be equal to what's in their respective jsons
	public static Map<Item, SuperNumber> seedEmcMap = new HashMap<Item, SuperNumber>();
	public static Map<Item, SuperNumber> customEmcMap = new HashMap<Item, SuperNumber>();

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
		if (emcMap.containsKey(item))
			return new SuperNumber(emcMap.get(item));
		return SuperNumber.Zero(); 
	}
	public static SuperNumber getItemStackEmc(ItemStack itemStack) {
		if (itemStack.isEmpty())
			return SuperNumber.Zero(); 
		Item item = itemStack.getItem();
		SuperNumber emc = getItemEmc(item);
		emc.multiply(itemStack.getCount());
		considerStackDurability(itemStack, emc);
		considerStackNbt(itemStack, emc);
		return emc;
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

		if (!ModDataFiles.NBT_ITEMS.hasItem(Registries.ITEM.getId(item).toString()))
			return;

		if (item instanceof PotionItem) {
			String potionName = nbt.getString("Potion");
			Potion potion = Registries.POTION.get(new Identifier(potionName));
			if (potionEmcMap.containsKey(potion)) {

				SuperNumber addition = potionEmcMap.get(potion);
				if (!addition.equalsZero())
					emc.add(addition);
			}   
		}
		else if (item == Items.ENCHANTED_BOOK) {
			NbtList enchantments = nbt.getList("StoredEnchantments", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < enchantments.size(); i++) {
				NbtCompound enchantmentCompound = enchantments.getCompound(i);
				String enchantmentId = enchantmentCompound.getString("id");
				Enchantment enchantment = Registries.ENCHANTMENT.get(new Identifier(enchantmentId));
				if (enchantment == null)
					continue;
				
				SuperNumber enchantmentEmc;
				enchantmentEmc = enchantmentEmcMap.containsKey(enchantment) 
					? new SuperNumber(enchantmentEmcMap.get(enchantment)) 
					: new SuperNumber(32);
			
				
				int repairCost = nbt.getInt("RepairCost");
				// anvil uses = log2(repairCost)
				int anvilUses = 32 - Integer.numberOfLeadingZeros(repairCost);
				// (x/7-1)^2
				SuperNumber repairCostPenalty = new SuperNumber(anvilUses, 7);
				repairCostPenalty.subtract(BigInteger.ONE);
				repairCostPenalty.square();		  
				
				int level = enchantmentCompound.getInt("lvl");
				
				/* an enchantment level n is worth as much as
					2^(n-1) * cost of enchantment level 1
					+ (2^(n-1)-1) * the cost of an enchanted book

					derived from anvil book combining. please trust me
				*/

				SuperNumber enchantedBookCost = EmcData.getItemEmc(item);
				enchantedBookCost.multiply((1 << (level - 1)) - 1);
				
				enchantmentEmc.multiply(1 << (level - 1));
				enchantmentEmc.add(enchantedBookCost);
				enchantmentEmc.multiply(repairCostPenalty);
				enchantmentEmc.ceil();
				emc.add(enchantmentEmc);
			}
		}
		else if (nbt.contains("BlockEntityTag")) { // shulker box
			NbtList list = nbt.getCompound("BlockEntityTag").getList("Items", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < list.size(); i++) {
				NbtCompound itemCompound = list.getCompound(i);
				String id = itemCompound.getString("id");
				ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier(id)));
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
		return seedEmcMap.containsKey(item);
	}
	public static boolean isItemInCustomValues(Item item) {
		return customEmcMap.containsKey(item);
	}

	// only the server can use these
	public static SuperNumber getEmc(LivingEntity player) {
		PlayerState playerState = ServerState.getPlayerState(player);
		return playerState.emc;
	}

	public static void setItemEmc(Item item, SuperNumber emc, boolean seed) {
		if (item == null)
			return;
		DataFile<Map<Item, SuperNumber>> file = seed ? ModDataFiles.SEED_EMC_MAP : ModDataFiles.CUSTOM_EMC_MAP;

		//String id = Registries.ITEM.getId(item).toString();
		Map<Item, SuperNumber> newEmcMap = file.getCopy();
		if (newEmcMap == null)
			newEmcMap = new HashMap<Item, SuperNumber>();
		newEmcMap.put(item, emc);
		file.setValueAndSave(newEmcMap);
		
		synchronized (emcMap) {
			GeneralUtil.mergeMap(emcMap, newEmcMap);
		}
		
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
		// send all the maps over
		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeInt(EmcData.emcMap.keySet().size());
		for (Item s : EmcData.emcMap.keySet()) {
			buffer.writeIdentifier(Registries.ITEM.getId(s));
			buffer.writeString(EmcData.emcMap.get(s).divisionString());
		}
		buffer.writeInt(EmcData.potionEmcMap.keySet().size());
		for (Potion s : EmcData.potionEmcMap.keySet()) {
			buffer.writeIdentifier(Registries.POTION.getId(s));
			buffer.writeString(EmcData.potionEmcMap.get(s).divisionString());
		}
		buffer.writeInt(EmcData.enchantmentEmcMap.keySet().size());
		for (Enchantment s : EmcData.enchantmentEmcMap.keySet()) {
			buffer.writeIdentifier(Registries.ENCHANTMENT.getId(s));
			buffer.writeString(EmcData.enchantmentEmcMap.get(s).divisionString());
		}

		ServerPlayNetworking.send(player, ModMessages.EMC_MAP_SYNC_IDENTIFIER, buffer);
	}
}

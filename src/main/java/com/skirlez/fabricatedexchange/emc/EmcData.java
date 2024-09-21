package com.skirlez.fabricatedexchange.emc;

import com.google.common.collect.ImmutableMap;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.NbtItem;
import com.skirlez.fabricatedexchange.packets.ModServerToClientPackets;
import com.skirlez.fabricatedexchange.util.PlayerState;
import com.skirlez.fabricatedexchange.util.ServerState;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.EmcMapFile;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.math.BigInteger;
import java.util.Map;

public class EmcData {

	public static volatile ImmutableMap<Item, SuperNumber> emcMap = ImmutableMap.of();
	public static volatile ImmutableMap<Potion, SuperNumber> potionEmcMap = ImmutableMap.of();
	public static volatile ImmutableMap<Enchantment, SuperNumber> enchantmentEmcMap = ImmutableMap.of();

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
		if (nbt.contains(EmcStoringItem.EMC_NBT_KEY))
			emc.add(new SuperNumber(nbt.getString(EmcStoringItem.EMC_NBT_KEY)));

		if (!ModDataFiles.NBT_ITEMS.hasItem(Registries.ITEM.getId(item).toString()))
			return;
		
		if (item instanceof PotionItem) {
			String potionName = nbt.getString("Potion");
			Potion potion = Registries.POTION.get(new Identifier(potionName));
			if (potionEmcMap.containsKey(potion)) {
				SuperNumber addition = potionEmcMap.get(potion);
				emc.add(addition);
			}
		}
		else if (item instanceof TippedArrowItem) {
			String potionName = nbt.getString("Potion");
			Potion potion = Registries.POTION.get(new Identifier(potionName));
			if (potionEmcMap.containsKey(potion)) {
				SuperNumber addition = new SuperNumber(potionEmcMap.get(potion));
				addition.divide(8);
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
		return ModDataFiles.SEED_EMC_MAP.hasItem(item);
	}
	public static boolean isItemInCustomValues(Item item) {
		return ModDataFiles.CUSTOM_EMC_MAP.hasItem(item);
	}

	// only the server can use these
	public static SuperNumber getEmc(LivingEntity player) {
		PlayerState playerState = ServerState.getPlayerState(player);
		return playerState.emc;
	}

	public static void setItemEmc(Item item, SuperNumber emc, boolean seed) {
		EmcMapFile file = seed ? ModDataFiles.SEED_EMC_MAP : ModDataFiles.CUSTOM_EMC_MAP;

		Map<String, String> newEmcMap = file.getCopy();
		newEmcMap.put(Registries.ITEM.getId(item).toString(), emc.divisionString());
		file.setValueAndSave(newEmcMap);
	}
	
	public static void removeItemEmc(Item item, boolean seed) {
		EmcMapFile file = seed ? ModDataFiles.SEED_EMC_MAP : ModDataFiles.CUSTOM_EMC_MAP;

		Map<String, String> newEmcMap = file.getCopy();
		newEmcMap.remove(Registries.ITEM.getId(item).toString());
		file.setValueAndSave(newEmcMap);
	}
	public static void setEmc(ServerPlayerEntity player, SuperNumber amount) {
		PlayerState playerState = ServerState.getPlayerState(player);
		playerState.emc = amount;
		ModServerToClientPackets.UPDATE_PLAYER_EMC.send(player, playerState.emc);
	}	
	public static void addEmc(ServerPlayerEntity player, SuperNumber amount) {
		PlayerState playerState = ServerState.getPlayerState(player);
		playerState.emc.add(amount);
		ModServerToClientPackets.UPDATE_PLAYER_EMC.send(player, playerState.emc);
	}	
	public static void subtractEmc(ServerPlayerEntity player, SuperNumber amount) {
		PlayerState playerState = ServerState.getPlayerState(player);
		playerState.emc.subtract(amount);
		ModServerToClientPackets.UPDATE_PLAYER_EMC.send((ServerPlayerEntity)player, playerState.emc);
	}	


}

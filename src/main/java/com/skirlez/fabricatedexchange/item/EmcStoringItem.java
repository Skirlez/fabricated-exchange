package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/** Used for items that can store a variable amount of EMC alongside their worth: batteries, rings, etc */
public interface EmcStoringItem {
	public static final String EMC_NBT_KEY = "emc";
	public static final String NO_EMC_TRANSLATION_KEY = "item.fabricated-exchange.no_emc";

	public static SuperNumber getStoredEmc(ItemStack stack) {
		if (stack.getNbt() == null)
			return SuperNumber.Zero();
		String emc = stack.getNbt().getString(EMC_NBT_KEY);
		if (emc.isEmpty())
			return SuperNumber.Zero();
		return new SuperNumber(emc);
	}
	
	public static void setStoredEmc(ItemStack stack, SuperNumber emc) {
		stack.getOrCreateNbt().putString(EMC_NBT_KEY, emc.divisionString());
	}
	
	public static void addStoredEmc(ItemStack stack, SuperNumber emc) {
		SuperNumber sum = getStoredEmc(stack);
		sum.add(emc);
		setStoredEmc(stack, sum);
	}
	private static boolean isItemStackConsumable(ItemStack stack) {
		return (FabricatedExchange.fuelSet.contains(stack.getItem()));
	}
	public static SuperNumber getTotalConsumableEmc(Inventory inv) {
		SuperNumber sum = SuperNumber.Zero();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			SuperNumber emc = EmcData.getItemStackEmc(stack);
			if (isItemStackConsumable(stack))
				sum.add(emc);
		}
		return sum;
	}
	public static SuperNumber getTotalConsumableEmc(Inventory inv, ItemStack stack) {
		SuperNumber emc = getTotalConsumableEmc(inv);
		emc.add(getStoredEmc(stack));
		return emc;
	}
	
	/** @return Whether or not the amount can be consumed from this inventory. */
	public static boolean isAmountConsumable(SuperNumber amount, PlayerInventory inv) {
		if (inv.player.isCreative())
			return true;
		SuperNumber sum = SuperNumber.Zero();
		for (int i = 0; i < inv.size() && sum.compareTo(amount) == -1; i++) {
			ItemStack stack = inv.getStack(i);
			SuperNumber emc = EmcData.getItemStackEmc(stack);
			if (isItemStackConsumable(stack))
				sum.add(emc);
		}
		return sum.compareTo(amount) != -1;
	}
	/** @return Whether or not the amount can be consumed from this inventory and itemstack combined. */
	public static boolean isAmountConsumable(SuperNumber amount, ItemStack stack, PlayerInventory inv) {
		if (inv.player.isCreative())
			return true;
		SuperNumber sum = getStoredEmc(stack);
		for (int i = 0; i < inv.size() && sum.compareTo(amount) == -1; i++) {
			ItemStack itemStack = inv.getStack(i);
			SuperNumber emc = EmcData.getItemStackEmc(itemStack);
			if (isItemStackConsumable(itemStack))
				sum.add(emc);
		}
		return sum.compareTo(amount) != -1;
	}
	
	/** Consumes the desired amount of EMC from the inventory, prioritizing batteries.
	 * @return The amount of EMC stored in the stack after the operation, or 0 if there isn't enough EMC in the inventory. */
	public static SuperNumber tryConsumeEmc(SuperNumber desiredAmount, ItemStack stack, PlayerInventory inv) {
		if (inv.player.isCreative()) {
			addStoredEmc(stack, desiredAmount);
			return new SuperNumber(desiredAmount);
		}
		if (!isAmountConsumable(desiredAmount, inv))
			return SuperNumber.Zero();
		SuperNumber sum = SuperNumber.Zero();
		for (int i = 0; i < inv.size() && sum.compareTo(desiredAmount) == -1; i++) {
			ItemStack invStack = inv.getStack(i);
			if (isItemStackConsumable(invStack)) {
				SuperNumber emc = EmcData.getItemStackEmc(invStack);
				
				SuperNumber sumCopy = new SuperNumber(sum);
				sumCopy.add(emc);
	
				if (sumCopy.compareTo(desiredAmount) != -1) {
					// Consuming this entire stack would get us the desired amount,
					// but we don't want all of it - just enough to go over or to the desired amount
					SuperNumber unitEmc = new SuperNumber(emc);
					unitEmc.divide(invStack.getCount());
					
					SuperNumber unitsNeeded = new SuperNumber(desiredAmount);
					unitsNeeded.subtract(sum);
					unitsNeeded.divide(unitEmc);

					// We can't take a fraction of an item. We have to take the next whole number to get over the desired amount.
					unitsNeeded.ceil();

					int units = unitsNeeded.toInt(0);
					
					unitEmc.multiply(units);
					sum.add(unitEmc);

					invStack.decrement(units);
					break;
				}
				
				inv.setStack(i, Items.AIR.getDefaultStack());
				sum.add(emc);
			}

		}
		addStoredEmc(stack, sum);
		return getStoredEmc(stack);
		
	}
	
	/** Tries taking EMC from the item.
	 * If there isn't enough, this method will try consuming from the inventory via tryConsumeEmc.
	 * 
	 * @return false if there wasn't enough EMC stored in the item and provided inventory, or true if the amount was successfully taken. */
	public static boolean takeStoredEmcOrConsume(SuperNumber amount, ItemStack stack, PlayerInventory inv) {
		SuperNumber emc = getStoredEmc(stack);
		if (emc.compareTo(amount) != -1) {
			SuperNumber copy = new SuperNumber(amount);
			copy.negate();
			addStoredEmc(stack, copy);
			return true;
		}
		
		
		SuperNumber requiredDifference = new SuperNumber(amount);
		requiredDifference.subtract(emc);
		
		if (!isAmountConsumable(requiredDifference, inv))
			return false;
		
		tryConsumeEmc(requiredDifference, stack, inv);
		SuperNumber copy = new SuperNumber(amount);
		copy.negate();
		addStoredEmc(stack, copy);
		return true;
	}

	@Environment(EnvType.CLIENT)
	public static void showNoEmcMessage() {
		GeneralUtil.showOverlayMessage(Text.translatable(NO_EMC_TRANSLATION_KEY));
	}
	public static void sendNoEmcMessage(ServerPlayerEntity player) {
		GeneralUtil.sendOverlayMessage(player, Text.translatable(NO_EMC_TRANSLATION_KEY));
	}
}
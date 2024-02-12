package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Used for items that can store a variable amount of EMC alongside their worth: batteries, rings, etc */
public interface EmcStoringItem {
	public static final String EMC_NBT_KEY = "emc";
	
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
	
	private static SuperNumber getTotalConsumableEmc(Inventory inv) {
		SuperNumber sum = SuperNumber.Zero();
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getStack(i);
			SuperNumber emc = EmcData.getItemStackEmc(stack);
			if (isItemStackConsumable(stack))
				sum.add(emc);
		}
		return sum;
	}

	private static boolean isAmountConsumable(SuperNumber amount, Inventory inv) {
		SuperNumber sum = SuperNumber.Zero();
		for (int i = 0; i < inv.size() && sum.compareTo(amount) == -1; i++) {
			ItemStack stack = inv.getStack(i);
			SuperNumber emc = EmcData.getItemStackEmc(stack);
			if (isItemStackConsumable(stack))
				sum.add(emc);
		}
		return sum.compareTo(amount) != -1;
	}
	
	/** Consumes the desired amount of EMC from the inventory, prioritizing batteries.
	 * @returns The amount of EMC stored in the stack after the operation, or 0 if there isn't enough EMC in the inventory. */
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
					int decrease = 0;
					while (sum.compareTo(desiredAmount) == -1) {
						sum.add(unitEmc);
						decrease++;
					}
					
					invStack.decrement(decrease);
					break;
				}
				
				inv.setStack(i, Items.AIR.getDefaultStack());
				sum.add(emc);
			}

		}
		addStoredEmc(stack, sum);
		return getStoredEmc(stack);
		
	}

	
}
package com.skirlez.fabricatedexchange.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;

public interface ItemWithModes {
	int getModeAmount();


	public static final String MODE_KEY = "FE_Mode";

	/** Cycle through the ItemStack's mode, modifying its NBT.
	 * @return The item's new mode. */
	public static int cycleModes(ItemStack stack) {
		if (!(stack.getItem() instanceof ItemWithModes item))
			return 0;
		int newMode = (getMode(stack) + 1) % item.getModeAmount();
		stack.getOrCreateNbt().putInt(MODE_KEY, newMode);
		return newMode;
	}

	public static int getMode(ItemStack stack) {
		if (!(stack.getItem() instanceof ItemWithModes item))
			return 0;
		NbtCompound nbt = stack.getNbt();
		if (nbt == null)
			return 0;
		int mode = nbt.getInt(MODE_KEY);
		if (mode >= item.getModeAmount())
			return item.getModeAmount() - 1;
		return mode;
	}
	public static <T extends Enum<T>> T getMode(ItemStack stack, T[] values) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null)
			return values[0];
		int mode = nbt.getInt(MODE_KEY);
		if (values.length <= mode)
			return values[0];
		return values[nbt.getInt(MODE_KEY)];
	}

	public static void addModeToTooltip(ItemStack stack, List<Text> tooltip) {
		int mode = getMode(stack);
		tooltip.add(Text.translatable("item.fabricated-exchange.mode_switch")					
					.append(" ")
					.append(getModeName(stack, mode).setStyle(Style.EMPTY.withColor(Formatting.GOLD))));
	}


	default boolean modeSwitchCondition(ItemStack stack) {
		return true;
	}

	public static MutableText getModeName(ItemStack stack, int mode) {
		return Text.translatable(stack.getTranslationKey() + ".mode_" + (mode + 1)); // additional + 1 because mode translation keys are 1 indexed 
	}


}

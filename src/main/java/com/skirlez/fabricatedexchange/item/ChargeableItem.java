package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.sound.ModSounds;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;

public interface ChargeableItem {
	public static final String CHARGE_KEY = "Charge";
	public static final int COLOR = MathHelper.packRgb(0.1f, 0.47f, 0.82f);

	public static int getCharge(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null)
			return 0;
		return nbt.getInt(CHARGE_KEY);
	}

	public static void chargeStack(ItemStack stack, int value, int min, int max, PlayerEntity player) {
		NbtCompound stackNbt = stack.getOrCreateNbt();
		int oldValue = stackNbt.getInt(CHARGE_KEY);
		int newValue = oldValue + value;
		if (newValue > max)
			newValue = max;
		else if (newValue < min)
			newValue = min;
		if (oldValue != newValue) {
			stackNbt.putInt(CHARGE_KEY, newValue);
			SoundEvent sound = (Math.signum(value) == 1) ? ModSounds.ITEM_CHARGE : ModSounds.ITEM_DISCHARGE;

			if (player.world.isClient()) {
				((ClientPlayerEntity)player).playSound(sound, 
					SoundCategory.PLAYERS, 1, 0.5F + ((0.5F / (float) max) * oldValue));
			}
			else {
				player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(), sound, 
					SoundCategory.PLAYERS, 1, 0.5F + ((0.5F / (float) max) * oldValue));
			}

			ChargeableItem item = (ChargeableItem)stack.getItem();
			item.onCharge(stack, newValue, player);
		}
	}

	default int getMaxCharge() {
		return 4;
	}

	public static int getItemBarStep(ItemStack stack, int maxCharge) {
		int charge = getCharge(stack);
		return Math.round((float)charge * 13.0f / (float)maxCharge);
	}

	default void onCharge(ItemStack stack, int charge, PlayerEntity player) {

	}



}

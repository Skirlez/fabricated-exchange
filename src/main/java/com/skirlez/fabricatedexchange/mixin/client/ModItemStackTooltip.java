package com.skirlez.fabricatedexchange.mixin.client;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreen;
import com.skirlez.fabricatedexchange.screen.slot.transmutation.TransmutationSlot;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import com.skirlez.fabricatedexchange.util.config.ModDataFiles;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ModItemStackTooltip {
	@Shadow
	int count;
	
	@Inject(method = "getTooltip", at = @At("RETURN"))
	protected void addEmcToTooltip(CallbackInfoReturnable<ArrayList<Text>> cir) {
		ItemStack itemStack = (ItemStack)(Object)this;
		ArrayList<Text> list = cir.getReturnValue();
		SuperNumber emc = EmcData.getItemEmc(itemStack.getItem());
		if (emc.equalsZero()) 
			return;
		EmcData.considerStackNbt(itemStack, emc);
		if (emc.equalsZero())
			return;
		int maxCount = itemStack.getMaxCount();
		MinecraftClient client = MinecraftClient.getInstance();

		EmcData.considerStackDurability(itemStack, emc);
		
		if (ModDataFiles.MAIN_CONFIG_FILE.showEnchantedBookRepairCost && itemStack.getItem().equals(Items.ENCHANTED_BOOK)) {
			NbtCompound nbt = itemStack.getNbt();
			if (nbt != null) {
				int repairCost = nbt.getInt("RepairCost");
				if (repairCost != 0)
					list.add(Text.literal("§6Added Repair Cost: " + repairCost + "§r"));
			}
		}

		if (client.currentScreen instanceof TransmutationTableScreen currentScreen
				&& currentScreen.getFocusedSlot() instanceof TransmutationSlot 
				&& Screen.hasShiftDown() && maxCount != 1) {
			SuperNumber itemCount = new SuperNumber(FabricatedExchangeClient.clientEmc);
			itemCount.divide(emc);
			itemCount.floor();

			SuperNumber sMaxCount = new SuperNumber(maxCount);
			sMaxCount = SuperNumber.min(sMaxCount, itemCount);
			if (sMaxCount.equalsOne())
				list.add(Text.literal("§eEMC§r: " + emc));
			else {
				emc.multiply(sMaxCount);
				list.add(Text.literal("§eEMC for " + sMaxCount + ": §r" + emc));
			}
		}
		else {
			list.add(Text.literal("§eEMC§r: " + emc));
			if (count > 1) {
				emc.multiply(count);
				list.add(Text.literal("§eStack EMC: §r" + emc));
			}
			if (itemStack.getNbt() != null) {
				String storedEmcString = itemStack.getNbt().getString("emc");
				
				if (!storedEmcString.isEmpty()) {
					SuperNumber storedEmc = new SuperNumber(storedEmcString);
					list.add(Text.literal("§eStored EMC§r: " + storedEmc));
				}
			}
		}
		
		 
		if (ModDataFiles.MAIN_CONFIG_FILE.showItemEmcOrigin) {
			boolean seed = EmcData.isItemInSeedValues(itemStack.getItem());
			boolean custom = EmcData.isItemInCustomValues(itemStack.getItem());
			if (seed)
				list.add(Text.literal("§eSeed EMC"));
			if (custom)
				list.add(Text.literal("§eCustom EMC"));
			if (!seed && !custom && !emc.equalsZero())
				list.add(Text.literal("§eInferred EMC"));
		}
		
	}

	protected void showEmcOrigin(CallbackInfoReturnable<ArrayList<Text>> cir) {

	}
}

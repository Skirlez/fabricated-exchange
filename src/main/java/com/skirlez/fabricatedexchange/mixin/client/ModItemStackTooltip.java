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
import com.skirlez.fabricatedexchange.util.ConfigFile;
import com.skirlez.fabricatedexchange.util.ModConfig;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Mixin(ItemStack.class)
public class ModItemStackTooltip {
    @Shadow
    int count;
    
    @Inject(method = "getTooltip", at = @At("RETURN"))
	protected void addEmcToTooltip(CallbackInfoReturnable<ArrayList<Text>> cir) {
		ItemStack itemStack = (ItemStack)(Object)this;
		SuperNumber emc = EmcData.getItemEmc(itemStack.getItem());
		if (emc.equalsZero()) 
			return;
		ArrayList<Text> list = cir.getReturnValue();
		int maxCount = itemStack.getMaxCount();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen instanceof TransmutationTableScreen 
				&& (((TransmutationTableScreen)client.currentScreen)).getFocusedSlot() instanceof TransmutationSlot 
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
		}
		if (ModConfig.CONFIG_FILE.getOption(ConfigFile.Bool.SHOW_ITEM_EMC_ORIGIN)) {
			if (EmcData.isItemInSeedValues(itemStack.getItem()))
				list.add(Text.literal("§eSeed EMC"));
			if (EmcData.isItemInCustomValues(itemStack.getItem()))
				list.add(Text.literal("§eCustom EMC"));
		}
		
	}
}

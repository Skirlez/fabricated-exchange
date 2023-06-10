package com.skirlez.fabricatedexchange.mixin.client;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.FabricatedExchangeClient;
import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.screen.TransmutationTableScreen;
import com.skirlez.fabricatedexchange.screen.slot.TransmutationSlot;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
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

			int intItemCount = itemCount.toInt();
			if (intItemCount != 0)
				maxCount = Math.min(maxCount, intItemCount);

			emc.multiply(maxCount);
			list.add(Text.literal("§eEMC for " + maxCount + ": §r" + emc.toString()));
		}
		else {
			list.add(Text.literal("§eEMC§r: " + emc.toString()));
			if (count > 1) {
				emc.multiply(count);
				list.add(Text.literal("§eStack EMC: §r" + emc.toString()));
			}
		}
			
		
	}


}

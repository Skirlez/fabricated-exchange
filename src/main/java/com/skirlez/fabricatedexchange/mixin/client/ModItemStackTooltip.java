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
		ArrayList<Text> list = cir.getReturnValue();
		SuperNumber emc = EmcData.getItemEmc(itemStack.getItem());
		if (!emc.equalsZero()) {
			int maxCount = itemStack.getMaxCount();
			MinecraftClient client = MinecraftClient.getInstance();
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
				if (itemStack.getMaxDamage() != 0) {
					emc.multiply(new SuperNumber(itemStack.getMaxDamage()-itemStack.getDamage(), itemStack.getMaxDamage()));
					emc.floor();
				}
				
				
				list.add(Text.literal("§eEMC§r: " + emc));
				if (count > 1) {
					emc.multiply(count);
					list.add(Text.literal("§eStack EMC: §r" + emc));
				}
			}
		}
		if (ModConfig.CONFIG_FILE.showItemEmcOrigin) {
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
}

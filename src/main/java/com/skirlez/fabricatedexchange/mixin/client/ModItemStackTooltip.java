package com.skirlez.fabricatedexchange.mixin.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.interfaces.ModItemStackInterface;
import com.skirlez.fabricatedexchange.util.SuperNumber;

@Mixin(ItemStack.class)
public class ModItemStackTooltip implements ModItemStackInterface {
	@Shadow
	private int count;


	// If this is not zero (and shift is down), the item will display how much emc it is for a stack as large as it's maxCount value.
	// it is an integer because, somewhere along placing the item in the transmutation slot and it displaying, the game
	// copies the item two times, which would wipe this value for the new item. the copy method makes sure it can be copied 3 times
	// (another one for your inventory) before removing this property. very stupid, might remove this feature if i can come up with
	// a less insulting implementation
	public int displayMaxStack = 0;

	public SuperNumber getEmc() {
		ItemStack itemStack = (ItemStack)(Object)this;
		SuperNumber emc = EmcData.getItemEmc(itemStack.getItem());
		return emc;
	}
	
	public void setDisplayMaxStack(int set) {
		displayMaxStack = set;
	}
	public int getDisplayMaxStack() {
		return displayMaxStack;
	}

	@Inject(method = "getTooltip", at = @At("RETURN"))
	protected void injectTooltipMethod(CallbackInfoReturnable<ArrayList<Text>> cir) {
		SuperNumber emc = getEmc();
		if (!emc.equalsZero()) {
			ArrayList<Text> list = cir.getReturnValue();
			if (cir != null) {
				if (displayMaxStack != 0 && Screen.hasShiftDown()) {
					ItemStack itemStack = (ItemStack)(Object)this;
					int maxCount = itemStack.getMaxCount();
					if (maxCount != 1) {
						emc.multiply(maxCount);
						list.add(Text.literal("§eEMC for " + maxCount + ": §r" + emc.toString()));
					}
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
	};
	
	@Inject(method = "copy", at = @At("RETURN"))
	public void injectCopy(CallbackInfoReturnable<ItemStack> cir) {
		if (displayMaxStack == 0)
			return;
		ModItemStackInterface itemStack = (ModItemStackInterface)(Object)cir.getReturnValue();
		itemStack.setDisplayMaxStack(displayMaxStack - 1);
		return;
	};
	
}
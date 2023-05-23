package com.skirlez.fabricatedexchange.mixin;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.util.ModItemInterface;
import com.skirlez.fabricatedexchange.util.SuperNumber;

@Mixin(ItemStack.class)
public abstract class ModItemEMC implements ModItemInterface {
	@Shadow
	private int count;
	
	private SuperNumber baseEMC = SuperNumber.Zero();
	

	private SuperNumber fetchEMC() {
		ItemStack itemStack = (ItemStack)(Object)this;
		return FabricatedExchange.getItemEmc(itemStack.getItem());
	}

	public SuperNumber getEMC() {
		SuperNumber baseEmcCopy = new SuperNumber(baseEMC);
		baseEmcCopy.multiply(count);
		return baseEmcCopy;
	}

	public SuperNumber getBaseEMC() {
		SuperNumber baseEmcCopy = new SuperNumber(baseEMC);
		return baseEmcCopy;
	}




	@Inject(method = "getTooltip", at = @At("RETURN"))
	protected void injectTooltipMethod(CallbackInfoReturnable<ArrayList<Text>> cir) {
		SuperNumber emc = getEMC();
		if (!emc.equalsZero()) {
			ArrayList<Text> list = cir.getReturnValue();
			if (cir != null) {
				list.add(Text.literal("§eEMC§r: " + baseEMC.toString(3)));
				if (count > 1) 
					list.add(Text.literal("§eStack EMC: §r" + emc.toString(3)));
			}

			
		}
	};
	@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At(value = "TAIL"))
	protected void injectItemStackConstructor(ItemConvertible item, int count, CallbackInfo info) {
		baseEMC = fetchEMC();
	};

}
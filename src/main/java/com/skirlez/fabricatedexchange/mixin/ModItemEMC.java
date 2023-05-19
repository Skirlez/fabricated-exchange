package com.skirlez.fabricatedexchange.mixin;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.math.BigInteger;
import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.skirlez.fabricatedexchange.FabricatedExchange;

@Mixin(ItemStack.class)
public class ModItemEMC {
	private BigInteger baseEMC = BigInteger.ZERO;

	private BigInteger fetchEMC() {
		ItemStack itemStack = (ItemStack)(Object)this;
		return FabricatedExchange.getItemEmc(itemStack.getItem());
	}

	public BigInteger getEMC() {
		ItemStack itemStack = (ItemStack)(Object)this;
		return baseEMC.multiply(BigInteger.valueOf(itemStack.getCount()));
	}



	@Inject(method = "getTooltip", at = @At("RETURN"))
	protected void injectTooltipMethod(CallbackInfoReturnable<ArrayList<Text>> cir) {
		BigInteger emc = getEMC();
		if (!emc.equals(BigInteger.ZERO)) {
			ArrayList<Text> list = cir.getReturnValue();
			if (cir != null)
				list.add(Text.empty().append("EMC: " + emc));
		}
	};
	@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At(value = "TAIL"))
	protected void injectItemStackConstructor(ItemConvertible item, int count, CallbackInfo info) {
		baseEMC = fetchEMC();
	};

}
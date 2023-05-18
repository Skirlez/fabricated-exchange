package com.skirlez.fabricatedexchange.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.math.BigInteger;
import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.skirlez.fabricatedexchange.FabricatedExchange;

@Mixin(ItemStack.class)
public class ModItemEMC {
	// TODO mixin into constructor
	private BigInteger baseEMC = BigInteger.ZERO;
	private boolean fetchedEMC = false;

	private BigInteger fetchEMC() {
		ItemStack itemStack = (ItemStack)(Object)this;
		return FabricatedExchange.getItemEmc(itemStack.getItem());
	}

	public BigInteger getEMC() {
		if (!fetchedEMC) {
			baseEMC = fetchEMC();
			fetchedEMC = true;
		}
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
}
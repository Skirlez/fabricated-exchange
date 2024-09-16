package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.tools.base.FEHammer;
import net.minecraft.item.*;

import net.minecraft.item.ItemStack;

public class DarkMatterHammer extends FEHammer {

	public DarkMatterHammer(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
		super(material, attackDamage, attackSpeed, settings);
	}

	@Override
	public int getModeAmount() {
		return 2;
	}

	@Override
	public boolean modeSwitchCondition(ItemStack stack) {
		return ChargeableItem.getCharge(stack) != 0;
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		return true;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		return ChargeableItem.COLOR;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		return ChargeableItem.getItemBarStep(stack, getMaxCharge());
	}

	@Override
	public int getMaxCharge() {
		return 3;
	}

}
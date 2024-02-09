package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.item.BaseFunction.FlyingAbilityItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;




public class SwiftWolfsRendingGale extends FlyingAbilityItem
		implements ExtraFunctionItem, ItemWithModes {

	public SwiftWolfsRendingGale(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}

	@Override
	public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
		// Implement the specific extra function logic here
		stack.getOrCreateNbt().putInt("CustomModelData", 1);
	}

	@Override
	public int getModeAmount() {
		return 3;
	}

	@Override
	public boolean modeSwitchCondition(ItemStack stack) {
		return true;
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		player.getAbilities().allowFlying = false;
		player.getAbilities().flying = false;
		return false;
	}
}


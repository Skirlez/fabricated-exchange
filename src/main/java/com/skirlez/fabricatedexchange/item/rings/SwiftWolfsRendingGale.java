package com.skirlez.fabricatedexchange.item.rings;

import java.util.List;

import org.joml.Vector2d;
import org.joml.Vector3d;

import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.item.tools.DarkMatterSword;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;




public class SwiftWolfsRendingGale extends FlyingAbilityItem
		implements ExtraFunctionItem, ItemWithModes {

	public static final String FLYING_MODEL_KEY = "CustomModelData";
	
	public SwiftWolfsRendingGale(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}

	@Override
	public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
		// Implement the specific extra function logic here
		// stack.getOrCreateNbt().putInt("CustomModelData", 1);
	}

	@Override
	public int getModeAmount() {
		return 2;
	}

	@Override
	public boolean modeSwitchCondition(ItemStack stack) {
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (entity instanceof ServerPlayerEntity player) {
			if (player.getAbilities().flying)
				stack.getOrCreateNbt().putInt(FLYING_MODEL_KEY, 1);
			else
				stack.getOrCreateNbt().putInt(FLYING_MODEL_KEY, 0);
			
			if (!world.isClient && ItemWithModes.getMode(stack) == 1) {
				List<Entity> entities = player.getWorld()
					.getOtherEntities(player, GeneralUtil.boxAroundPos(player.getPos(), 8));
				
				for (Entity otherEntity : entities) {
					Vec3d velocity = otherEntity.getPos().subtract(player.getPos()).normalize().multiply(0.2);
					otherEntity.addVelocity(velocity);
				}
			}
		}
	}
	
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		ItemWithModes.addModeToTooltip(stack, tooltip);
	}
	
	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		player.getAbilities().allowFlying = false;
		player.getAbilities().flying = false;
		return false;
	}

	public static boolean isOn(ItemStack stack) {
		return stack.getNbt() == null ? false : stack.getNbt().getInt(FLYING_MODEL_KEY) == 1 ? true : false;
	}
	
	public static boolean isRepelling(ItemStack stack) {
		return ItemWithModes.getMode(stack) == 1 ? true : false;
	}
	
}


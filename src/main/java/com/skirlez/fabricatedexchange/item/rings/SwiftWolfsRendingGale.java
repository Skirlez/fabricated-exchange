package com.skirlez.fabricatedexchange.item.rings;

import java.math.BigInteger;
import java.util.List;

import com.skirlez.fabricatedexchange.entities.TornadoThrownEntity;
import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;

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
		implements ExtraFunctionItem, ItemWithModes, EmcStoringItem {

	public static final String FLYING_MODEL_KEY = "CustomModelData";
	
	public SwiftWolfsRendingGale(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}

	
	private static final SuperNumber PROJECTILE_COST = new SuperNumber(128);
	
	@Override
	public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {

		if (!player.getItemCooldownManager().isCoolingDown(this)
				&& EmcStoringItem.takeStoredEmcOrConsume(PROJECTILE_COST, stack, player.getInventory())) {
			World world = player.world;
			Vec3d direction = player.getRotationVec(1.0F);

			TornadoThrownEntity projectile = new TornadoThrownEntity(player, world);
			projectile.setVelocity(direction.x, direction.y, direction.z, 2.5F, 0F);

			world.spawnEntity(projectile);

			player.getItemCooldownManager().set(this, 10);
		}
	}

	@Override
	public int getModeAmount() {
		return 2;
	}

	@Override
	public ItemStack getDefaultStack() {
		ItemStack stack = new ItemStack(this);
		stack.getOrCreateNbt().putString(EmcStoringItem.EMC_NBT_KEY, "0");
		return stack;
	}
	
	private static final SuperNumber DESIRED_AMOUNT = new SuperNumber(64);
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);

		if (entity instanceof PlayerEntity player) {
			SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);
			boolean shouldSubtract = player.age % 3 == 0;
			
			
			if (player.getAbilities().flying) {
				stack.getOrCreateNbt().putInt(FLYING_MODEL_KEY, 1);
				if (shouldSubtract)
					storedEmc.subtract(BigInteger.ONE);
			}
			else
				stack.getOrCreateNbt().putInt(FLYING_MODEL_KEY, 0);
			
			if (ItemWithModes.getMode(stack) == 1) {
				
				if (!world.isClient) {
					List<Entity> entities = player.getWorld()
						.getOtherEntities(player, GeneralUtil.boxAroundPos(player.getPos(), 8));
					
					for (Entity otherEntity : entities) {
						Vec3d velocity = otherEntity.getPos().subtract(player.getPos()).normalize().multiply(0.2);
						otherEntity.addVelocity(velocity);
					}
				}
				if (shouldSubtract)
					storedEmc.subtract(BigInteger.ONE);
			}
			
			if (storedEmc.equalsZero())
				storedEmc = EmcStoringItem.tryConsumeEmc(DESIRED_AMOUNT, stack, player.getInventory());
			EmcStoringItem.setStoredEmc(stack, storedEmc);
		}
	}
	
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		ItemWithModes.addModeToTooltip(stack, tooltip);
	}
	
	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		disablePlayerFlying(player, stack);
		return false;
	}

	public static boolean isOn(ItemStack stack) {
		return stack.getNbt() != null && (stack.getNbt().getInt(FLYING_MODEL_KEY) == 1);
	}
	
	public static boolean isRepelling(ItemStack stack) {
		return ItemWithModes.getMode(stack) == 1;
	}
	
	@Override
	protected boolean flyCondition(PlayerEntity player, ItemStack stack) {
		return EmcStoringItem.getStoredEmc(stack).isPositive();
	}
	@Override
	protected void onFlightDisable(PlayerEntity player, ItemStack stack) {
		stack.getOrCreateNbt().putInt(FLYING_MODEL_KEY, 0);
	}
	@Override
	public void onDropped(PlayerEntity player, ItemStack stack) {
		stack.getOrCreateNbt().putInt(FLYING_MODEL_KEY, 0);
		super.onDropped(player, stack);
	}

}


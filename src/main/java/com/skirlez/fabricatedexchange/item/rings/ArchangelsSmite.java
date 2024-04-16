package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;


public class ArchangelsSmite extends Item
		implements ExtraFunctionItem, ItemWithModes, EmcStoringItem {

	private static final SuperNumber DESIRED_AMOUNT = new SuperNumber(98);

	public boolean autoshoot = false;

	public ArchangelsSmite(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}

	@Override
	public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
		autoshoot = !autoshoot;
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
	public ItemStack getDefaultStack() {
		ItemStack stack = new ItemStack(this);
		stack.getOrCreateNbt().putString(EmcStoringItem.EMC_NBT_KEY, "0");
		return stack;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);

		arrowFireLogic(world, user, stack);

		return TypedActionResult.success(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity pEntity, int slot, boolean selected) {
		SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);
		SuperNumber arrowEMC = EmcData.getItemEmc(Items.ARROW);

		if (arrowEMC.equalsZero()) {
			arrowEMC = new SuperNumber(14);
		}

		if (pEntity instanceof PlayerEntity player) {
			if (storedEmc.toDouble() < (arrowEMC.toDouble()*7)) {
				storedEmc = EmcStoringItem.tryConsumeEmc(DESIRED_AMOUNT, stack, player.getInventory());

				if (storedEmc.toDouble() < arrowEMC.toDouble())
					return;
			}
			if (!storedEmc.isPositive())
				storedEmc = EmcStoringItem.tryConsumeEmc(DESIRED_AMOUNT, stack, player.getInventory());
			EmcStoringItem.setStoredEmc(stack, storedEmc);
		}

		if (autoshoot){
			stack.getOrCreateNbt().putInt("CustomModelData", 1);
			PlayerEntity user = (PlayerEntity) pEntity;
			arrowFireLogic(world, user, stack);
		} else{
			stack.getOrCreateNbt().putInt("CustomModelData", 0);
		}
	}

	public void fireSingleArrow(World world, PlayerEntity user, float speed, float divergence){
		ArrowEntity arrow = new ArrowEntity(world, user);
		arrow.updatePosition(user.getX(), user.getEyeY() - 0.1, user.getZ());
		Vec3d vec3d = user.getRotationVec(1.0F);
		arrow.setVelocity(vec3d.x, vec3d.y, vec3d.z, speed, divergence);
		arrow.pickupType = ArrowEntity.PickupPermission.DISALLOWED;
		world.spawnEntity(arrow);
	}

	public boolean fireHomingArrow(World world, PlayerEntity user, float speed, float divergence) {
		var triggered = false;
		// Get all entities within a certain radius around the player, excluding the player
		if (!user.getItemCooldownManager().isCoolingDown(this)) {
			List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, user.getBoundingBox().expand(20), entity -> entity != user);
			LivingEntity closestEntity = null;
			double closestDistance = Double.MAX_VALUE;

			for (LivingEntity entity : entities) {
				double distance = user.squaredDistanceTo(entity);
				if (distance < closestDistance && isVisible(user, entity, world)) {
					closestEntity = entity;
					closestDistance = distance;
				}
			}

			// If a closest visible entity was found, shoot an arrow towards it
			if (closestEntity != null) {
				Vec3d direction = closestEntity.getPos().subtract(user.getPos().add(0, 0.5d, 0)).normalize();
				ArrowEntity arrow = new ArrowEntity(world, user);
				arrow.updatePosition(user.getX(), user.getEyeY() - 0.1, user.getZ());
				arrow.setVelocity(direction.x, direction.y, direction.z, speed, divergence);
				arrow.pickupType = ArrowEntity.PickupPermission.DISALLOWED;
				world.spawnEntity(arrow);
				user.getItemCooldownManager().set(this, 5);
				triggered = true;
			}
		}
		return triggered;
	}

	private boolean isVisible(PlayerEntity user, LivingEntity target, World world) {
		Vec3d userEyes = user.getCameraPosVec(1.0F); // Player's eye position
		Vec3d targetEyes = target.getPos().add(0, target.getEyeHeight(target.getPose()), 0); // Target's eye position
		// Create a raycast context from the player to the target
		RaycastContext context = new RaycastContext(userEyes, targetEyes, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user);
		// Perform the ray trace
		BlockHitResult result = world.raycast(context);
		// Check if the ray trace hit a block before reaching the target
		return result.getType() == HitResult.Type.MISS || result.getPos().squaredDistanceTo(targetEyes) < 0.5;
	}

	public void arrowFireLogic(World world, PlayerEntity user, ItemStack stack){
		SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);
		int mode = ItemWithModes.getMode(stack);

		if (!world.isClient) {
			var arrowEMC = EmcData.getItemEmc(Items.ARROW);
			if (arrowEMC.equalsZero()) {
				arrowEMC = new SuperNumber(14);
			}

			switch (mode) {
				case 0: {
					if (storedEmc.toDouble() >= arrowEMC.toDouble() && !user.getItemCooldownManager().isCoolingDown(this)) {
						storedEmc.subtract(arrowEMC);
						fireSingleArrow(world, user, 5.0F, 0.0F);
						user.getItemCooldownManager().set(this, 5);
					}
					break;
				}
				case 1: {
					if (storedEmc.toDouble() >= (arrowEMC.toDouble()*7) && !user.getItemCooldownManager().isCoolingDown(this)) {
						for (int i = 0; i < 7; i++) {
							storedEmc.subtract(arrowEMC);
							fireSingleArrow(world, user, 3.5F, 6.0F);
						}
						user.getItemCooldownManager().set(this, 10);
					}
					break;
				}
				case 2: {
					if (storedEmc.toDouble() >= arrowEMC.toDouble()*2){
						SuperNumber doubleArrowEMC = arrowEMC;
						doubleArrowEMC.multiply(2);
						if (fireHomingArrow(world, user, 3.5f, 0.0f)){
							storedEmc.subtract(doubleArrowEMC);
						}
					}
					break;
				}
			}
		}
		EmcStoringItem.setStoredEmc(stack, storedEmc); // Save updated EMC outside the switch
	}

	public static boolean isAngry(ItemStack stack) {
		return ItemWithModes.getMode(stack) == 2;
	}
	
}


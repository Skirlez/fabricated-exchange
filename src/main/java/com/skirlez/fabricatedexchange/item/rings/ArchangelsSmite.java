package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.rings.base.ShooterRing;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ArchangelsSmite extends ShooterRing implements ItemWithModes {

	public ArchangelsSmite(Settings settings) {
		super(settings);
	}

	private SuperNumber getProjectileCost() {
		return EmcData.getItemEmc(Items.ARROW);
	}



	protected boolean consumeEmcAndFireProjectile(ItemStack stack, PlayerEntity player, Vec3d direction, World world) {
		if (!EmcStoringItem.takeStoredEmcOrConsume(getProjectileCost(), stack, player.getInventory()))
			return false;

		ArrowEntity arrow = new ArrowEntity(world, player);
		arrow.updatePosition(player.getX(), player.getEyeY() - 0.1, player.getZ());
		arrow.setVelocity(direction.x, direction.y, direction.z, 3f, 0f);
		// There's actually no reason why this should be disallowed, technically... you did pay for those
		arrow.pickupType = ArrowEntity.PickupPermission.DISALLOWED;
		world.spawnEntity(arrow);

		return true;
	}

	@Override
	protected void playShootSound(PlayerEntity player, World world) {
		world.playSound(null, player.getX(), player.getY(), player.getZ(),
			SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0f,
			1.0f / (world.getRandom().nextFloat() * 0.4f + 1.2f) + 0.5f);
	}



	/*
	@Override
	protected void fireSingleProjectile(World world, PlayerEntity user, float speed, float divergence) {
		ArrowEntity arrow = new ArrowEntity(world, user);
		arrow.updatePosition(user.getX(), user.getEyeY() - 0.1, user.getZ());
		Vec3d vec3d = user.getRotationVec(1.0F);
		arrow.setVelocity(vec3d.x, vec3d.y, vec3d.z, speed, divergence);
		arrow.pickupType = ArrowEntity.PickupPermission.DISALLOWED;
		world.spawnEntity(arrow);
	}

	@Override
	protected boolean fireHomingProjectile(World world, PlayerEntity user, float speed, float divergence) {
		var triggered = false;
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

			if (closestEntity != null) {
				Vec3d direction = closestEntity.getPos().subtract(user.getPos().offset(Direction.UP, 0.5)).normalize();
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

	@Override
	protected void fireChaosProjectile(World world, PlayerEntity user, float speed, float divergence) {
		Random random = new Random();

		Vec3d direction = new Vec3d(
				random.nextDouble() * 2 - 1,
				random.nextDouble() * 2 - 1,
				random.nextDouble() * 2 - 1
		).normalize();
		ArrowEntity arrow = new ArrowEntity(world, user);
		arrow.setPosition(user.getX() + direction.x * 2, user.getEyeY() - 1, user.getZ() + direction.z * 2);
		arrow.setVelocity(direction.x, direction.y, direction.z, speed, divergence);
		arrow.pickupType = ArrowEntity.PickupPermission.DISALLOWED;
		world.spawnEntity(arrow);
	}


	*/
}


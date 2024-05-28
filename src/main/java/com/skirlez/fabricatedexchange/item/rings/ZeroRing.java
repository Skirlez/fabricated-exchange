package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.projectiles.FrozenThrownEntity;
import com.skirlez.fabricatedexchange.item.rings.base.FEShooterRing;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ZeroRing extends FEShooterRing {
	
	public ZeroRing(Settings settings) {
		super(settings);
	}

	@Override
	protected SuperNumber getProjectileEMC() {
		return EmcData.getItemEmc(Items.POWDER_SNOW_BUCKET);
	}

	@Override
	protected void fireSingleProjectile(World world, PlayerEntity user, float speed, float divergence) {
		Vec3d direction = user.getRotationVec(1.0F).normalize();
		FrozenThrownEntity frozenProjectile = new FrozenThrownEntity(world, user);
		frozenProjectile.setPosition(user.getX() + direction.x * 2, user.getEyeY() - 1, user.getZ() + direction.z * 2);
		frozenProjectile.setVelocity(direction.x, direction.y, direction.z, speed, divergence);
		world.spawnEntity(frozenProjectile);
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
				Vec3d direction = closestEntity.getPos().subtract(user.getPos().offset(Direction.UP, 1)).normalize();
				FrozenThrownEntity frozenProjectile = new FrozenThrownEntity(world, user);
				frozenProjectile.setPosition(user.getX() + direction.x * 2, user.getEyeY() + direction.y, user.getZ() + direction.z * 2);
				frozenProjectile.setVelocity(direction.x, direction.y, direction.z, speed, divergence);
				world.spawnEntity(frozenProjectile);
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
		FrozenThrownEntity frozenProjectile = new FrozenThrownEntity(world, user);
		frozenProjectile.setPosition(user.getX() + direction.x * 2, user.getEyeY() - 1, user.getZ() + direction.z * 2);
		frozenProjectile.setVelocity(direction.x, direction.y, direction.z, speed, divergence);
		world.spawnEntity(frozenProjectile);
	}
}

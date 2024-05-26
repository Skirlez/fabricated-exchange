package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.rings.base.FEShooterRing;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class IgnitionRing extends FEShooterRing {

	public IgnitionRing(Settings settings) {
		super(settings);
	}

	@Override
	protected SuperNumber getProjectileEMC() {
		return EmcData.getItemEmc(Items.FIRE_CHARGE);
	}

	@Override
	protected void fireSingleProjectile(World world, PlayerEntity user, float speed, float divergence) {
		Vec3d direction = user.getRotationVec(1.0F).normalize();
		SmallFireballEntity fireball = new SmallFireballEntity(world, user, direction.x, direction.y, direction.z);
		fireball.setPosition(user.getX() + direction.x * 2, user.getEyeY() - 1, user.getZ() + direction.z * 2);
		fireball.setVelocity(direction.x, direction.y, direction.z, speed, divergence);
		world.spawnEntity(fireball);
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
				SmallFireballEntity fireball = new SmallFireballEntity(world, user, direction.x, direction.y, direction.z);
				fireball.setPosition(user.getX() + direction.x * 2, user.getEyeY() + direction.y, user.getZ() + direction.z * 2);
				fireball.setVelocity(direction.x, direction.y, direction.z, speed, divergence);
				world.spawnEntity(fireball);
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
		SmallFireballEntity fireball = new SmallFireballEntity(world, user, direction.x, direction.y, direction.z);
		fireball.setPosition(user.getX() + direction.x * 2, user.getEyeY() - 1, user.getZ() + direction.z * 2);
		fireball.setVelocity(direction.x, direction.y, direction.z, speed, divergence);
		world.spawnEntity(fireball);
	}

	@Override
	protected void applyPlayerEffects(PlayerEntity player) {
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3, 0, true, false, false));
		if (player.isOnFire()) {
			player.extinguish();
		}
	}
}

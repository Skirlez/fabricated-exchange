package com.skirlez.fabricatedexchange.entities;

import com.skirlez.fabricatedexchange.entities.base.ZeroGravityThrownEntity;
import com.skirlez.fabricatedexchange.item.ModItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class FrozenThrownEntity extends ZeroGravityThrownEntity {

	public FrozenThrownEntity(EntityType<? extends ZeroGravityThrownEntity> entityType, World world) {
		super(entityType, world);
	}

	public FrozenThrownEntity(World world, LivingEntity owner) {
		super(ModEntities.FROZEN_PROJECTILE, owner, world);
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		if (hitResult.getType() == HitResult.Type.ENTITY) {
			EntityHitResult entityHitResult = (EntityHitResult) hitResult;
			Entity entity = entityHitResult.getEntity();

			// Apply freezing effect
			if (entity instanceof LivingEntity livingEntity) {
				livingEntity.setFrozenTicks(1000);
				livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 3));
				livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 10, 1));
				livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 10, 2));
			}
		}

		this.discard();
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.FROZEN_ORB;
	}

}
package com.skirlez.fabricatedexchange.entities;

import com.skirlez.fabricatedexchange.entities.base.ZeroGravityThrownEntity;
import com.skirlez.fabricatedexchange.item.ModItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TornadoThrownEntity extends ZeroGravityThrownEntity {

	public TornadoThrownEntity(EntityType<? extends ZeroGravityThrownEntity> entityType, World world) {
		super(entityType, world);
	}

	public TornadoThrownEntity(LivingEntity owner, World world) {
		super(ModEntities.TORNADO_PROJECTILE, owner, world);
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);

		if (!this.world.isClient) {

		}
	}

	@Override
	protected Item getDefaultItem() {
		// Return the item associated with this entity
		return ModItems.TORNADO_ORB;
	}

}
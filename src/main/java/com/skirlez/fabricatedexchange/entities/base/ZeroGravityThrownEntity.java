package com.skirlez.fabricatedexchange.entities.base;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.world.World;

public abstract class ZeroGravityThrownEntity extends ThrownItemEntity {
	private int age = 0;

	public ZeroGravityThrownEntity(EntityType<? extends ZeroGravityThrownEntity> entityType, World world) {
		super(entityType, world);
		this.setNoGravity(true);
	}

	public ZeroGravityThrownEntity(EntityType<? extends ZeroGravityThrownEntity> entityType, LivingEntity owner, World world) {
		super(entityType, owner, world);
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		super.tick();

		age++;
		if (age > 200) {
			this.remove(RemovalReason.DISCARDED);
		}
	}
}
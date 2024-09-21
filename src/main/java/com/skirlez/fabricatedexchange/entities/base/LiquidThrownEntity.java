package com.skirlez.fabricatedexchange.entities.base;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public abstract class LiquidThrownEntity extends ZeroGravityThrownEntity {
	protected int charge;

	public LiquidThrownEntity(EntityType<? extends LiquidThrownEntity> entityType, World world) {
		super(entityType, world);
	}

	public LiquidThrownEntity(EntityType<? extends LiquidThrownEntity> entityType, LivingEntity owner, World world, int charge) {
		super(entityType, owner, world);
		this.charge = charge;
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		BlockPos pos;
		if (hitResult instanceof BlockHitResult blockHitResult)
			pos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
		else
			pos = BlockPos.ofFloored(hitResult.getPos());
		for (int x = -charge; x <= charge; x++) {
			for (int z = -charge; z <= charge; z++) {
				BlockPos offsetPos = pos.add(x, 0, z);
				placeLiquid(world, offsetPos);
			}
		}
		world.playSound(null, pos, getEmptyBucketSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
		this.world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
		this.discard();
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("charge", charge);
	}
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		charge = nbt.getInt("charge");
	}
	@Override
	public void handleStatus(byte status) {
		if (status != EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
			return;
		ParticleEffect particleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, new ItemStack(getDefaultItem()));
		Random random = world.getRandom();
		for (int i = 0; i < 8; i++) {
			this.world.addParticle(particleEffect, this.getX(), this.getY(), this.getZ(),
				(random.nextDouble() * 2d - 1d) * 0.2f,
				(random.nextDouble() * 2d - 1d) * 0.2f,
				(random.nextDouble() * 2d - 1d) * 0.2f);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (isRemoved())
			return;
		BlockPos pos = getBlockPos();
		boolean shouldPlaySound = false;
		if (charge != 0) {
			for (int x = -charge; x <= charge; x++) {
				for (int y = -charge; y <= charge; y++) {
					for (int z = -charge; z <= charge; z++) {
						BlockPos offsetPos = pos.add(x, y, z);
						shouldPlaySound = passByBlock(world, offsetPos) || shouldPlaySound;
					}
				}
			}
		}
		if (shouldPlaySound)
			this.world.playSound(null, pos, getPassModifySound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
	}



	protected abstract void placeLiquid(World world, BlockPos pos);

	/** @return true if block at that position was modified. */
	protected abstract boolean passByBlock(World world, BlockPos pos);
	protected abstract SoundEvent getEmptyBucketSound();
	protected abstract SoundEvent getPassModifySound();
}
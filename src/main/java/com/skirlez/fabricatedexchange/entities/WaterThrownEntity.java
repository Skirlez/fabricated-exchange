package com.skirlez.fabricatedexchange.entities;

import com.skirlez.fabricatedexchange.entities.base.LiquidThrownEntity;
import com.skirlez.fabricatedexchange.item.ModItems;

import net.minecraft.block.*;
import net.minecraft.client.sound.Sound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WaterThrownEntity extends LiquidThrownEntity {
	public WaterThrownEntity(EntityType<? extends LiquidThrownEntity> entityType, World world) {
		super(entityType, world);
	}

	public WaterThrownEntity(LivingEntity owner, World world, int charge) {
		super(ModEntities.WATER_PROJECTILE, owner, world, charge);
	}

	@Override
	protected void placeLiquid(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof CauldronBlock)
			world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
		if (state.getBlock() instanceof Waterloggable && state.contains(Properties.WATERLOGGED))
			world.setBlockState(pos, state.with(Properties.WATERLOGGED, true), 11);

		if (state.canBucketPlace(Fluids.WATER)) {
			world.setBlockState(pos, Blocks.WATER.getDefaultState(), 11);
		}
	}

	@Override
	protected boolean passByBlock(World world, BlockPos pos) {
		if (this.world.getFluidState(pos).getFluid() == Fluids.LAVA) {
			this.world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
			return true;
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (isRemoved())
			return;
		// TODO add config option for this
		if (this.getY() > 150 && this.getVelocity().y > 0) {
			if (world instanceof ServerWorld serverWorld) {

				if (world.isRaining()){
					serverWorld.setWeather(0, 6000, true, true);
				}
				else {
					serverWorld.setWeather(0, 6000, true, false);
				}

				this.discard();
			}
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult result) {

		/*
		Entity entity = result.getEntity();
		world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.NEUTRAL, 1.0f, 1.0f + (this.random.nextFloat()) * 0.4f);
		entity.extinguish();
		entity.setVelocity(this.getVelocity().normalize().negate().multiply(1.5d));

		this.world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
		this.discard();
		*/

	}


	protected SoundEvent getPassModifySound() {
		return SoundEvents.BLOCK_LAVA_EXTINGUISH;
	}
	protected SoundEvent getEmptyBucketSound() {
		return SoundEvents.ITEM_BUCKET_EMPTY;
	}

	@Override
	protected Item getDefaultItem() {
		// Return the item associated with this entity
		return ModItems.WATER_ORB;
	}
}
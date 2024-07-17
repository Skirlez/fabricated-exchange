package com.skirlez.fabricatedexchange.entities;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skirlez.fabricatedexchange.entities.base.LiquidThrownEntity;
import com.skirlez.fabricatedexchange.item.ModItems;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class LavaThrownEntity extends LiquidThrownEntity {

	public LavaThrownEntity(EntityType<? extends LiquidThrownEntity> entityType, World world) {
		super(entityType, world);
	}

	public LavaThrownEntity(World world, LivingEntity owner, int charge) {
		super(ModEntities.LAVA_PROJECTILE, owner, world, charge);
	}

	@Override
	protected void placeLiquid(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof CauldronBlock) {
			world.setBlockState(pos, Blocks.LAVA_CAULDRON.getDefaultState());
			// TODO: Spawn ash particle
		}
		if (state.getBlock() instanceof Waterloggable && state.contains(Properties.WATERLOGGED)) {
			world.setBlockState(pos, state.with(Properties.WATERLOGGED, false), 11);
			// TODO: Spawn ash particle
		}
		if (state.canBucketPlace(Fluids.LAVA)) {
			world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 11);
		}
	}

	@Override
	protected boolean passByBlock(World world, BlockPos pos) {
		if (this.world.getFluidState(pos).getFluid() == Fluids.WATER) {
			BlockState state = world.getBlockState(pos);
			if (state.contains(Properties.WATERLOGGED)
					&& state.get(Properties.WATERLOGGED)) {
				world.setBlockState(pos, state.with(Properties.WATERLOGGED, false), 11);
				return true;
			}
			this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
			return true;
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (isRemoved())
			return;

		if (this.getY() > 150 && this.getVelocity().y > 0) {
			if (world instanceof ServerWorld) {
				// TODO don't do it using commands
				/*
				ServerWorld serverWorld = (ServerWorld) world;
				ServerCommandSource commandSource = serverWorld.getServer().getCommandSource().withWorld(serverWorld);

				CommandDispatcher<ServerCommandSource> dispatcher = serverWorld.getServer().getCommandManager().getDispatcher();
				ParseResults<ServerCommandSource> parseResults = dispatcher.parse("weather clear", commandSource);
				dispatcher.execute(parseResults);
				*/
			}

			this.discard();
		}
		
	}



	protected SoundEvent getPassModifySound() {
		return SoundEvents.BLOCK_LAVA_EXTINGUISH;
	}
	protected SoundEvent getEmptyBucketSound() {
		return SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
	}


	@Override
	protected Item getDefaultItem() {
		return ModItems.LAVA_ORB;
	}
}
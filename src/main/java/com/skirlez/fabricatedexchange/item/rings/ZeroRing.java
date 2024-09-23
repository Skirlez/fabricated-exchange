package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.entities.base.FunctionalProjectile;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.rings.base.ShooterRing;
import com.skirlez.fabricatedexchange.util.ConstantObjectRegistry;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ZeroRing extends ShooterRing {
	
	public ZeroRing(Settings settings) {
		super(settings);
	}


	private static FunctionalProjectile.OnHit projectileHitBehavior
		= ConstantObjectRegistry.register("zero_ring_hit",
		(FunctionalProjectile self, HitResult result) -> {
			if (result instanceof EntityHitResult entityHitResult) {
				Entity entity = entityHitResult.getEntity();
				if (entity instanceof LivingEntity livingEntity) {
					livingEntity.setFrozenTicks(1000);
					livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 3));
					livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 10, 1));
					livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 10, 2));
				}
			}
			Random random = self.getWorld().getRandom();
			self.getWorld().playSound(null, self.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.NEUTRAL,
				0.6f, (2f * random.nextFloat() - 1f) * 0.2f + 1.2f);
			self.createDeathParticles(result);
		});

	@Override
	protected boolean consumeEmcAndFireProjectile(ItemStack stack, PlayerEntity player, Vec3d direction, World world) {
		if (!EmcStoringItem.takeStoredEmcOrConsume(getProjectileCost(), stack, player.getInventory()))
			return false;

		FunctionalProjectile projectile = FunctionalProjectile.builder(player, ModItems.FROZEN_ORB)
			.setMaxAge(400)
			.disableGravity()
			.setHitBehavior(projectileHitBehavior)
			.build();

		GeneralUtil.nudgeProjectileInDirection(projectile, direction);
		projectile.setVelocity(direction.multiply(2));
		world.spawnEntity(projectile);
		return true;
	}

	@Override
	protected void playShootSound(PlayerEntity player, World world) {
		Random random = world.getRandom();
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,
			0.4f, (2f * random.nextFloat() - 1f) * 0.2f + 2f);
	}


	private static SuperNumber FAILSAFE_COST = new SuperNumber(16);

	protected SuperNumber getProjectileCost() {
		SuperNumber powderSnowBucketEmc = EmcData.getItemEmc(Items.POWDER_SNOW_BUCKET);
		SuperNumber bucketEmc = EmcData.getItemEmc(Items.BUCKET);
		if (powderSnowBucketEmc.equalsZero() || bucketEmc.equalsZero())
			return FAILSAFE_COST;
		powderSnowBucketEmc.subtract(bucketEmc);
		return bucketEmc;
	}



}

package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.entities.base.FunctionalProjectile;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.rings.base.ShooterRing;
import com.skirlez.fabricatedexchange.util.ConstantObjectRegistry;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class IgnitionRing extends ShooterRing {
	public IgnitionRing(Settings settings) {
		super(settings);
	}

	@Override
	protected void applyPlayerEffects(PlayerEntity player) {
		player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3, 0, true, false, false));
		if (player.isOnFire()) {
			player.extinguish();
		}
	}

	public static FunctionalProjectile.OnHit projectileHitBehavior
		= ConstantObjectRegistry.register("ignition_hit",
		(FunctionalProjectile self, HitResult result) -> {
			if (result instanceof EntityHitResult entityHitResult) {
				Entity entity = entityHitResult.getEntity();
				entity.setOnFireFor(5);
				entity.damage(self.getDamageSources().onFire(), 5.0f);
				Vec3d push = self.getVelocity().multiply(1.0, 0.0, 1.0).normalize();
				entity.addVelocity(push.x, 0.1d, push.z);
			}
			else if (result instanceof BlockHitResult blockHitResult) {

				BlockPos blockPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
				if (AbstractFireBlock.canPlaceAt(self.getWorld(), blockPos, blockHitResult.getSide())) {
					BlockState blockState = AbstractFireBlock.getState(self.getWorld(), blockPos);
					self.getWorld().setBlockState(blockPos, blockState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
				}
			}
			Random random = self.getWorld().getRandom();
			for (SoundEvent event :
				new SoundEvent[] {SoundEvents.BLOCK_NETHERRACK_BREAK, SoundEvents.ITEM_FLINTANDSTEEL_USE}) {
				self.getWorld().playSound(null, self.getBlockPos(), event, SoundCategory.NEUTRAL,
					1f, (2f * random.nextFloat() - 1f) * 0.2f + 1f);
			}
			self.createDeathParticles(result);
		});

	@Override
	protected boolean consumeEmcAndFireProjectile(ItemStack stack, PlayerEntity player, Vec3d direction, World world) {
		if (!EmcStoringItem.takeStoredEmcOrConsume(getProjectileCost(), stack, player.getInventory()))
			return false;

		FunctionalProjectile projectile = FunctionalProjectile.builder(player, Items.FIRE_CHARGE)
			.setMaxAge(400)
			.setOnFire()
			.disableGravity()
			.setHitBehavior(projectileHitBehavior)
			.build();

		projectile.fire(world, direction.multiply(2));
		return true;
	}

	@Override
	protected void playShootSound(PlayerEntity player, World world) {
		Random random = world.getRandom();
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS,
			0.4f, (2f * random.nextFloat() - 1f) * 0.2f + 1.5f);
	}


	protected SuperNumber getProjectileCost() {
		return EmcData.getItemEmc(Items.FIRE_CHARGE);
	}
}

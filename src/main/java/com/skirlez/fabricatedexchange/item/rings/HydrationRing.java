package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.entities.base.FunctionalProjectile;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.rings.base.ShooterRing;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.ConstantObjectRegistry;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HydrationRing extends ShooterRing {

	public HydrationRing(Settings settings) {
		super(settings);
	}

	public static FunctionalProjectile.OnHit projectileHitBehavior
		= ConstantObjectRegistry.register("hydration_hit",
		(FunctionalProjectile self, HitResult result) -> {
			if (result instanceof EntityHitResult entityHitResult) {
				Entity entity = entityHitResult.getEntity();
				self.getWorld().playSound(null,
					entity.getX(), entity.getY(), entity.getZ(),
					SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.NEUTRAL,
					1.0f, 1.0f + (self.getWorld().getRandom().nextFloat()) * 0.4f);

				entity.extinguish();
				Vec3d velocity = self.getVelocity().normalize().negate().multiply(new Vec3d(1.5d, 1d, 1.5d));
				entity.setVelocity(velocity);
			}
			else if (result instanceof BlockHitResult blockHitResult) {
				Vec3d pos = blockHitResult.getPos();
				if (blockHitResult.isInsideBlock())
					self.setPosition(pos.x, blockHitResult.getPos().offset(blockHitResult.getSide().getOpposite(), 0.5d).getY(), pos.z);
			}
			self.getWorld().sendEntityStatus(self, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
			self.discard();
		});

	@Override
	protected boolean consumeEmcAndFireProjectile(ItemStack stack, PlayerEntity player, Vec3d direction, World world) {
		if (!EmcStoringItem.takeStoredEmcOrConsume(getProjectileCost(), stack, player.getInventory()))
			return false;

		FunctionalProjectile projectile = FunctionalProjectile.builder(player, ModItems.WATER_ORB, new NbtCompound())
			.disableGravity()
			.setMaxAge(400)
			.setHitBehavior(projectileHitBehavior)
			.build();

		projectile.setVelocity(direction.x, direction.y, direction.z, 2.5F, 0F);
		GeneralUtil.nudgeProjectileInDirection(projectile, direction);

		world.spawnEntity(projectile);
		return true;
	}

	@Override
	protected void playShootSound(PlayerEntity player, World world) {

		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}


	protected SuperNumber getProjectileCost() {
		return EmcData.getItemEmc(Items.FIRE_CHARGE);
	}
}

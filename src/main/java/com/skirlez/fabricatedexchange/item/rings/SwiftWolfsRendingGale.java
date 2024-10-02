package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.abilities.ItemAbility;
import com.skirlez.fabricatedexchange.entities.base.FunctionalProjectile;
import com.skirlez.fabricatedexchange.item.AbilityGrantingItem;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.sound.ModSoundEvents;
import com.skirlez.fabricatedexchange.util.ConstantObjectRegistry;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.math.BigInteger;
import java.util.List;


public class SwiftWolfsRendingGale extends Item
		implements ItemWithModes, EmcStoringItem, AbilityGrantingItem {

	public SwiftWolfsRendingGale(Settings settings) {
		super(settings);
	}

	private static final SuperNumber DESIRED_AMOUNT = new SuperNumber(16);

	private static final ItemAbility SWRG = new ItemAbility() {
		@Override
		public void tick(ItemStack stack, PlayerEntity player) {
			boolean shouldTick = player.age % 3 == 0;
			if (!shouldTick)
				return;

			SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);
			if (storedEmc.compareTo(SuperNumber.ONE) < 0) {
				storedEmc = EmcStoringItem.tryConsumeEmc(DESIRED_AMOUNT, stack, player.getInventory());
				if (storedEmc.compareTo(SuperNumber.ONE) < 0) {
					if (player.getAbilities().allowFlying) {
						player.getAbilities().allowFlying = false;
						player.getAbilities().flying = false;
						player.sendAbilitiesUpdate();
					}
					return;
				}

			}
			if (!player.getAbilities().allowFlying) {
				player.getAbilities().allowFlying = true;
				player.sendAbilitiesUpdate();
			}

			if (player.getAbilities().flying) {
				if (storedEmc.compareTo(SuperNumber.ONE) >= 0)
					storedEmc.subtract(BigInteger.ONE);
			}
			if (ItemWithModes.getMode(stack) == 1) {
				if (!player.getWorld().isClient) {
					List<Entity> entities = player.getWorld()
						.getOtherEntities(player, GeneralUtil.boxAroundPos(player.getPos(), 8));

					for (Entity otherEntity : entities) {
						Vec3d velocity = otherEntity.getPos().subtract(player.getPos()).normalize().multiply(0.2);
						otherEntity.addVelocity(velocity);
						otherEntity.velocityModified = true;
					}
				}
				if (storedEmc.compareTo(SuperNumber.ONE) >= 0)
					storedEmc.subtract(BigInteger.ONE);
			}
			if (storedEmc.compareTo(SuperNumber.ONE) < 0) {
				SuperNumber emc = EmcStoringItem.tryConsumeEmc(DESIRED_AMOUNT, stack, player.getInventory());
				storedEmc.add(emc);
			}
			EmcStoringItem.setStoredEmc(stack, storedEmc);
		}

		@Override
		public void onRemove(PlayerEntity player) {
			if (!player.getAbilities().creativeMode && !player.isSpectator()) {
				player.getAbilities().allowFlying = false;
				player.getAbilities().flying = false;
				player.sendAbilitiesUpdate();
			}
		}
	};

	@Override
	public ItemAbility getAbility() {
		return SWRG;
	}

	private static final SuperNumber PROJECTILE_COST = new SuperNumber(128);

	public static final FunctionalProjectile.OnHit projectileHitBehavior
		= ConstantObjectRegistry.register("swiftwolf_hit",
		(FunctionalProjectile self, HitResult result) -> {
			World world = self.getWorld();
			if (world.isThundering() || world.isRaining()) {
				BlockPos hitPos = BlockPos.ofFloored(result.getPos());
				int numberOfBolts = 1;
				if (world.isThundering()) {
					numberOfBolts = 3;
				}

				for (int i = 0; i < numberOfBolts; i++) {
					BlockPos offsetPos = hitPos.add(world.random.nextInt(3) - 1, 0, world.random.nextInt(3) - 1);
					LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);

					if (lightning != null) {
						lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(offsetPos));
						world.spawnEntity(lightning);
					}

				}
			}

			if (result instanceof EntityHitResult entityHitResult) {
				Entity entity = entityHitResult.getEntity();

				Vec3d velocity = self.getVelocity().normalize().multiply(4.0);
				entity.addVelocity(velocity.x, velocity.y + 1, velocity.z);
				entity.velocityModified = true;
			}
			Random random = world.getRandom();
			world.playSound(null, self.getBlockPos(), SoundEvents.BLOCK_SNOW_BREAK, SoundCategory.NEUTRAL,
				1.2f, (2f * world.getRandom().nextFloat() - 1f) * 0.2f + 1f);
			self.createDeathParticles(result, Items.SNOW.getDefaultStack());
		});


	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
	 	super.use(world, player, hand);
		ItemStack stack = player.getStackInHand(hand);
		if (!EmcStoringItem.takeStoredEmcOrConsume(PROJECTILE_COST, stack, player.getInventory()))
			return TypedActionResult.pass(stack);

		FunctionalProjectile projectile = FunctionalProjectile.builder(player, ModItems.TORNADO_ORB)
			.disableGravity()
			.setHitBehavior(projectileHitBehavior)
			.build();

		world.playSound(null, player.getBlockPos(), ModSoundEvents.WIND_PROJECTILE_FIRE, SoundCategory.NEUTRAL,
			0.6f, (2f * world.getRandom().nextFloat() - 1f) * 0.2f + 1.1f);

		Vec3d direction = GeneralUtil.getPlayerLookVector(player);
		projectile.fire(world, direction.multiply(2));
		return TypedActionResult.success(stack);
	}

	@Override
	public int getModeAmount() {
		return 2;
	}

	public static boolean isOn(LivingEntity entity, ItemStack stack) {
		return (entity instanceof PlayerEntity player && player.getAbilities().flying);
	}
	public static boolean isRepelling(ItemStack stack) {
		return ItemWithModes.getMode(stack) == 1;
	}
}


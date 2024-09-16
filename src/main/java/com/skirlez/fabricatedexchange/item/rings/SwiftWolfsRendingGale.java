package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.abilities.ItemAbility;
import com.skirlez.fabricatedexchange.entities.base.FunctionalProjectile;
import com.skirlez.fabricatedexchange.item.AbilityGrantingItem;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.util.ConstantObjectRegistry;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.math.BigInteger;
import java.util.List;


public class SwiftWolfsRendingGale extends Item
		implements ItemWithModes, EmcStoringItem, AbilityGrantingItem {

	public SwiftWolfsRendingGale(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}

	private static final SuperNumber DESIRED_AMOUNT = new SuperNumber(64);
	private static final ItemAbility SWRG = new ItemAbility() {
		@Override
		public void tick(ItemStack stack, PlayerEntity player) {
			if (!player.getAbilities().allowFlying) {
				player.getAbilities().allowFlying = true;
				player.sendAbilitiesUpdate();
			}
			boolean shouldSubtract = player.age % 3 == 0;
			SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);
			if (player.getAbilities().flying) {
				if (shouldSubtract && storedEmc.isPositive())
					storedEmc.subtract(BigInteger.ONE);
			}
			if (ItemWithModes.getMode(stack) == 1) {
				if (!player.getWorld().isClient) {
					List<Entity> entities = player.getWorld()
						.getOtherEntities(player, GeneralUtil.boxAroundPos(player.getPos(), 8));

					for (Entity otherEntity : entities) {
						Vec3d velocity = otherEntity.getPos().subtract(player.getPos()).normalize().multiply(0.2);
						otherEntity.addVelocity(velocity);
					}
				}
				if (shouldSubtract && storedEmc.isPositive())
					storedEmc.subtract(BigInteger.ONE);
			}
			if (!storedEmc.isPositive())
				storedEmc = EmcStoringItem.tryConsumeEmc(SuperNumber.ONE, stack, player.getInventory());
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

	@Override
	public boolean shouldGrantAbility(PlayerEntity player, ItemStack stack) {
		return (EmcStoringItem.getTotalConsumableEmc(player.getInventory(), stack).isPositive());
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
					// Slightly offset each lightning bolt to avoid exact overlap
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

				// Calculate the fling velocity
				Vec3d velocity = self.getVelocity().normalize().multiply(4.0);
				entity.addVelocity(velocity.x, velocity.y + 1, velocity.z);
				entity.velocityModified = true;
			}

			self.discard();
		});


	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
	 	super.use(world, player, hand);
		ItemStack stack = player.getStackInHand(hand);
		if (player.getItemCooldownManager().isCoolingDown(this)
				|| !EmcStoringItem.takeStoredEmcOrConsume(PROJECTILE_COST, stack, player.getInventory()))
			return TypedActionResult.pass(stack);


		FunctionalProjectile projectile = FunctionalProjectile.builder(player, ModItems.TORNADO_ORB, new NbtCompound())
			.disableGravity()
			.setMaxAge(400)
			.setHitBehavior(projectileHitBehavior)
			.build();

		Vec3d direction = GeneralUtil.getPlayerLookVector(player);
		projectile.setVelocity(direction.x, direction.y, direction.z, 2.5F, 0F);
		world.spawnEntity(projectile);
		player.getItemCooldownManager().set(this, 10);
		return TypedActionResult.success(stack);
	}

	@Override
	public int getModeAmount() {
		return 2;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		ItemWithModes.addModeToTooltip(stack, tooltip);
	}

	public static boolean isOn(LivingEntity entity, ItemStack stack) {
		return (entity instanceof PlayerEntity player && player.getAbilities().flying);
	}
	public static boolean isRepelling(ItemStack stack) {
		return ItemWithModes.getMode(stack) == 1;
	}
}


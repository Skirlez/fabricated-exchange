package com.skirlez.fabricatedexchange.item.rings.base;

import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class ShooterRing extends Item implements ExtraFunctionItem, ItemWithModes {

	private final Random random = new Random();

	public boolean autoshoot = false;

	public ShooterRing(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}

	@Override
	public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
		autoshoot = !autoshoot;
	}

	protected enum ShootMode {
		NORMAL,
		SHOTGUN,
		HOMING,
		CHAOS,

		SIZE
	}

	@Override
	public int getModeAmount() {
		return ShootMode.SIZE.ordinal();
	}

	@Override
	public ItemStack getDefaultStack() {
		ItemStack stack = new ItemStack(this);
		stack.getOrCreateNbt().putString(EmcStoringItem.EMC_NBT_KEY, "0");
		return stack;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		tryFireProjectile(stack, player, world);
		return TypedActionResult.success(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (entity instanceof PlayerEntity player) {
			applyPlayerEffects(player);
			if (autoshoot) {
				if (world.getTime() % 5 == 0)
					tryFireProjectile(stack, player, world);
			}
		}
	}

	private static final int SHOTGUN_AMOUNT = 7;
	protected void tryFireProjectile(ItemStack stack, PlayerEntity player, World world) {
		ShootMode mode = ItemWithModes.getMode(stack, ShootMode.values());

		switch (mode) {
			case NORMAL:
				if (consumeEmcAndFireProjectile(stack, player, GeneralUtil.getPlayerLookVector(player), world))
					playShootSound(player, world);
				break;
			case SHOTGUN:
				Vec3d lookDirection = GeneralUtil.getPlayerLookVector(player);

				double angle = 40d; // How wide an angle should the shot be

				double moveAngle = angle / (float)SHOTGUN_AMOUNT;

				lookDirection = lookDirection.rotateY((float)(-Math.toRadians(angle) / 2));
				boolean anySuccess = false;
				for (int i = 0; i < SHOTGUN_AMOUNT; i++) {
					if (!consumeEmcAndFireProjectile(stack, player, lookDirection, world))
						break;
					anySuccess = true;
					lookDirection = lookDirection.rotateY((float)Math.toRadians(moveAngle));
				}
				if (anySuccess)
					playShootSound(player, world);
				break;
			case HOMING:
				List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class, player.getBoundingBox().expand(20), entity -> entity != player);
				Optional<LivingEntity> closestEntity = entities.stream()
					.filter((entity) -> isVisible(player, entity, world))
					.min(Comparator.comparingDouble(player::squaredDistanceTo));

				if (closestEntity.isPresent()) {
					Vec3d direction = closestEntity.get().getPos().subtract(player.getPos().offset(Direction.UP, 0.5)).normalize();
					if (consumeEmcAndFireProjectile(stack, player, direction, world)) {
						playShootSound(player, world);
					}


					//player.getItemCooldownManager().set(this, 5);

				}

				break;
			case CHAOS:
				for (int i = 0; i < 7; i++) {
					Vec3d direction = new Vec3d(
						random.nextDouble() * 2 - 1,
						random.nextDouble() * 2 - 1,
						random.nextDouble() * 2 - 1
					).normalize();

					if (!consumeEmcAndFireProjectile(stack, player, direction, world))
						break;
				}
				break;

		}

	}


	protected void applyPlayerEffects(PlayerEntity player) {
		// Override to add specific player effects in subclasses
	}

	protected boolean isVisible(PlayerEntity user, LivingEntity target, World world) {
		Vec3d userEyes = user.getCameraPosVec(1.0F);
		Vec3d targetEyes = target.getPos().add(0, target.getEyeHeight(target.getPose()), 0);
		RaycastContext context = new RaycastContext(userEyes, targetEyes, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user);
		BlockHitResult result = world.raycast(context);
		return result.getType() == HitResult.Type.MISS || result.getPos().squaredDistanceTo(targetEyes) < 0.5;
	}

	/** Should returns true if successful, and false otherwise */
	protected abstract boolean consumeEmcAndFireProjectile(ItemStack stack, PlayerEntity player, Vec3d direction, World world);

	protected abstract void playShootSound(PlayerEntity player, World world);


}


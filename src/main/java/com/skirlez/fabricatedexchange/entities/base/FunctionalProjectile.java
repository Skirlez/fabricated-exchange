package com.skirlez.fabricatedexchange.entities.base;


import com.skirlez.fabricatedexchange.entities.ModEntities;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.packets.ExtendedVanillaPackets;
import com.skirlez.fabricatedexchange.util.ConstantObjectRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/**
 * A functional projectile class.
 * Projectile entities should be created and be given behavior through the builder.
 * @see FunctionalProjectile.Builder
 */
public class FunctionalProjectile extends ThrownItemEntity {
	private NbtCompound parameters;

	private int maxAge = 400;
	private boolean discardOnHit = true;
	private OnTick onTick = EMPTY_TICK;
	private OnHit onHit = EMPTY_HIT;
	private boolean visualFire = false;

	public static final OnTick EMPTY_TICK =
		ConstantObjectRegistry.register("fp_empty_tick", (p) -> {});
	public static final OnHit EMPTY_HIT =
		ConstantObjectRegistry.register("fp_empty_hit", (p, h) -> {});

	public FunctionalProjectile(EntityType<? extends FunctionalProjectile> entityType, World world) {
		super(entityType, world);
	}
	public FunctionalProjectile(LivingEntity owner, Item item, NbtCompound parameters) {
		super(ModEntities.FUNCTIONAL_PROJECTILE, owner, owner.getWorld());
		this.parameters = parameters;
	}

	@Override
	public boolean doesRenderOnFire() {
		return visualFire;
	}

	@Override
	public void tick() {
		super.tick();
		if (isRemoved())
			return;
		if (maxAge != 0) {
			age++;
			if (age > maxAge) {
				this.discard();
				return;
			}
		}

		// Undo speed penalties from super.tick()
		float h;
		if (this.isTouchingWater()) {
			h = 0.8f;
		}
		else {
			h = 0.99f;
		}
		this.setVelocity(getVelocity().multiply(1 / h));


		onTick.evaluate(this);
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		if (isRemoved() || getWorld().isClient())
			return;

		onHit.evaluate(this, hitResult);
		if (discardOnHit)
			discard();
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.DARK_MATTER;
	}


	private void writeExtraData(NbtCompound nbt) {
		nbt.put("parameters", parameters);
		nbt.putInt("maxAge", maxAge);
		nbt.putBoolean("discardOnHit", discardOnHit);
		nbt.putBoolean("visualFire", visualFire);
		nbt.putString("onTickId", ConstantObjectRegistry.getObjectId(onTick).orElse(""));
		nbt.putString("onHitId", ConstantObjectRegistry.getObjectId(onHit).orElse(""));
	}
	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		writeExtraData(nbt);
	}

	private void readExtraData(NbtCompound nbt) {
		parameters = nbt.getCompound("parameters");
		maxAge = nbt.getInt("maxAge");
		visualFire = nbt.getBoolean("visualFire");
		discardOnHit = nbt.getBoolean("discardOnHit");
		onTick = ConstantObjectRegistry.<OnTick>getObject(nbt.getString("onTickId")).orElse(EMPTY_TICK);
		onHit = ConstantObjectRegistry.<OnHit>getObject(nbt.getString("onHitId")).orElse(EMPTY_HIT);
	}
	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		readExtraData(nbt);
	}

	private void getSurfaceHitPos() {

	}

	public void createDeathParticles(HitResult result) {
		if (!(world instanceof ServerWorld serverWorld))
			return;
		Vec3d pos = result.getPos();
		if (result instanceof BlockHitResult blockHitResult) {
			if (blockHitResult.isInsideBlock())
				pos = new Vec3d(pos.x, blockHitResult.getPos().offset(blockHitResult.getSide().getOpposite(), 0.5d).getY(), pos.z);
		}
		ParticleEffect particleEffect = new ItemStackParticleEffect(ParticleTypes.ITEM, getItem());
		Random random = world.getRandom();
		for (int i = 0; i < 8; i++) {
			serverWorld.spawnParticles(particleEffect, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.2d);
		}
	}


	public static Builder builder(LivingEntity owner, Item item, NbtCompound parameters) {
		return new Builder(owner, item, parameters);
	}
	public static Builder builder(LivingEntity owner, Item item) {
		return new Builder(owner, item, new NbtCompound());
	}

	public static class Builder {
		FunctionalProjectile projectile;
		public Builder(LivingEntity owner, Item item, NbtCompound parameters) {
			projectile = new FunctionalProjectile(owner, item, parameters);
			projectile.setItem(new ItemStack(item));
		}

		/** The lambda provided with this function must be registered in the ConstantObjectRegistry.
		 * This lambda will execute on both client and server.
		 * @see com.skirlez.fabricatedexchange.util.ConstantObjectRegistry */
		public Builder setTickBehavior(OnTick onTick) {
			projectile.onTick = onTick;
			assert (ConstantObjectRegistry.getObjectId(onTick)).isPresent();

			return this;
		}

		/** The lambda provided with this function must be registered in the ConstantObjectRegistry.
		 * This lambda will only execute on the server.
		 * @see com.skirlez.fabricatedexchange.util.ConstantObjectRegistry */
		public Builder setHitBehavior(OnHit onHit) {
			projectile.onHit = onHit;
			assert (ConstantObjectRegistry.getObjectId(onHit)).isPresent();

			return this;
		}

		public Builder setDiscardOnHit(boolean discardOnHit) {
			projectile.discardOnHit = discardOnHit;
			return this;
		}

		public Builder setMaxAge(int maxAge) {
			projectile.maxAge = maxAge;
			return this;
		}
		public Builder disableGravity() {
			projectile.setNoGravity(true);
			return this;
		}
		public Builder setOnFire() {
			projectile.visualFire = true;
			return this;
		}
		public FunctionalProjectile build() {
			return projectile;
		}
	}

	@FunctionalInterface
	public static interface OnTick {
		public void evaluate(FunctionalProjectile projectile);
	}
	@FunctionalInterface
	public static interface OnHit {
		public void evaluate(FunctionalProjectile projectile, HitResult result);
	}

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket() {
		NbtCompound extraData = new NbtCompound();
		writeExtraData(extraData);
		return new ExtendedVanillaPackets.ExtraDataEntitySpawnS2CPacket(this, extraData);
	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);
		NbtCompound extraData = ((ExtendedVanillaPackets.ExtraDataEntitySpawnS2CPacket)packet).getExtraData();
		readExtraData(extraData);
	}
}

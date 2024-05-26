package com.skirlez.fabricatedexchange.item.projectiles;

import com.skirlez.fabricatedexchange.item.ModEntities;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.projectiles.base.FEThrownEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TornadoThrownEntity extends FEThrownEntity {

    public TornadoThrownEntity(EntityType<? extends FEThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    public TornadoThrownEntity(World world, LivingEntity owner) {
        super(ModEntities.TORNADO_PROJECTILE, world, owner);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.world.isClient) {
            // Check if it's raining and thunderstorming
            if (this.world.isThundering() || this.world.isRaining()) {
                // Get the position from the HitResult and convert to BlockPos
                Vec3d hitVec = hitResult.getPos();
                BlockPos hitPos = new BlockPos((int) hitVec.x, (int) hitVec.y, (int) hitVec.z);
                int numberOfBolts = 1;

                if (this.world.isThundering()){
                    numberOfBolts = 3;
                }

                for (int i = 0; i < numberOfBolts; i++) {
                    // Slightly offset each lightning bolt to avoid exact overlap
                    BlockPos offsetPos = hitPos.add(this.random.nextInt(3) - 1, 0, this.random.nextInt(3) - 1);

                    // Summon lightning at the offset position
                    LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(this.world);
                    if (lightning != null) {
                        lightning.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(offsetPos));
                        this.world.spawnEntity(lightning);
                    }
                }
            }

            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();

                // Calculate the fling velocity
                Vec3d velocity = this.getVelocity().normalize().multiply(4.0);
                entity.addVelocity(velocity.x, velocity.y + 1, velocity.z);
                entity.velocityModified = true;
            }

            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        // Return the item associated with this entity
        return ModItems.TORNADO_ORB;
    }

    @Override
    protected void handleTick(World world, BlockPos pos) {
        // Handle the entity tick logic
    }
}
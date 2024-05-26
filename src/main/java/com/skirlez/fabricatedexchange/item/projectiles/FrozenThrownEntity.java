package com.skirlez.fabricatedexchange.item.projectiles;

import com.skirlez.fabricatedexchange.item.ModEntities;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.projectiles.base.FEThrownEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FrozenThrownEntity extends FEThrownEntity {

    public FrozenThrownEntity(EntityType<? extends FEThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    public FrozenThrownEntity(World world, LivingEntity owner) {
        super(ModEntities.FROZEN_PROJECTILE, world, owner);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.world.isClient) {

            }

            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();

                // Apply freezing effect
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    entity.setFrozenTicks(1000);
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 3));
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 10, 1));
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 10, 2));
                }
            }

            this.discard();
    }


    @Override
    protected Item getDefaultItem() {
        // Return the item associated with this entity
        return ModItems.FROZEN_ORB;
    }

    @Override
    protected void handleTick(World world, BlockPos pos) {
        // Handle the entity tick logic
    }
}
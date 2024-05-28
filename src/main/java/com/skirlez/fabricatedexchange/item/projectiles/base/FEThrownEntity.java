package com.skirlez.fabricatedexchange.item.projectiles.base;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class FEThrownEntity extends ThrownItemEntity {
    private int age = 0;

    public FEThrownEntity(EntityType<? extends FEThrownEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public FEThrownEntity(EntityType<? extends FEThrownEntity> entityType, World world, LivingEntity owner) {
        super(entityType, owner, world);
        this.setNoGravity(true);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        // Remove the projectile entity
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.world.isClient) {
            // Handle specific logic on entity tick with error handling
            try {
                handleTick(world, this.getBlockPos());
            } catch (Exception e) {
                // Log the error and prevent the crash
                System.err.println("Error during tick handling: " + e.getMessage());
                e.printStackTrace();
            }
        }

        age++;
        if (age > 400) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
    }

    @Override
    protected Item getDefaultItem() {
        // Returns a default item like snowball for rendering purposes
        return Items.SNOWBALL;
    }

    protected abstract void handleTick(World world, BlockPos pos);

}
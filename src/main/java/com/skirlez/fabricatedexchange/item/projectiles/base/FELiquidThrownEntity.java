package com.skirlez.fabricatedexchange.item.projectiles.base;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class FELiquidThrownEntity extends ThrownItemEntity {
    protected Fluid liquid;
    protected int charge;
    private int age = 0;

    public FELiquidThrownEntity(EntityType<? extends FELiquidThrownEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public FELiquidThrownEntity(EntityType<? extends FELiquidThrownEntity> entityType, World world, LivingEntity owner, Fluid liquid, int charge) {
        super(entityType, owner, world);
        this.liquid = liquid;
        this.charge = charge;
        this.setNoGravity(true);
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!this.world.isClient) {
            BlockPos pos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());

            BlockState targetBlockState = world.getBlockState(blockHitResult.getBlockPos());
            if (targetBlockState.getBlock() instanceof CauldronBlock || targetBlockState.getBlock() instanceof Waterloggable) {
                pos = blockHitResult.getBlockPos();
            }

            // Calculate the radius of the square (1 for 3x3, 2 for 5x5, etc.)
            int radius = charge;

            // Loop through positions in the square
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos offsetPos = pos.add(x, 0, z);
                    BlockState blockState = world.getBlockState(offsetPos);

                    // Custom liquid handling logic with error handling
                    try {
                        handleLiquidPlacement(world, offsetPos, blockState);
                    } catch (Exception e) {
                        // Log the error and prevent the crash
                        System.err.println("Error placing liquid: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            world.playSound(null, blockHitResult.getBlockPos(), getEmptyBucketSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);

            // Remove the projectile entity
            this.remove(RemovalReason.DISCARDED);
        }
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
        nbt.putString("Liquid", Registries.FLUID.getId(this.liquid).toString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.liquid = Registries.FLUID.get(new Identifier(nbt.getString("Liquid")));
    }

    @Override
    protected Item getDefaultItem() {
        // Returns a default item like snowball for rendering purposes
        return Items.SNOWBALL;
    }

    public void setLiquid(Fluid liquid) {
        this.liquid = liquid;
    }

    protected abstract void handleLiquidPlacement(World world, BlockPos pos, BlockState state);

    protected abstract void handleTick(World world, BlockPos pos) throws CommandSyntaxException;

    protected abstract SoundEvent getEmptyBucketSound();
}
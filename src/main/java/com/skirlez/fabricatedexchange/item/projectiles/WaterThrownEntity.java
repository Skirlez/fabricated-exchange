package com.skirlez.fabricatedexchange.item.projectiles;

import com.skirlez.fabricatedexchange.item.ModEntities;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.projectiles.base.FELiquidThrownEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WaterThrownEntity extends FELiquidThrownEntity {
    private int mode;

    public WaterThrownEntity(EntityType<? extends FELiquidThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    public WaterThrownEntity(World world, LivingEntity owner, int charge, int mode) {
        super(ModEntities.WATER_PROJECTILE, world, owner, Fluids.WATER, charge);
        this.mode = mode;
    }

    @Override
    protected void handleLiquidPlacement(World world, BlockPos pos, BlockState state) {
        if (state == null || pos == null) {
            return;
        }

        // Check if the block is a cauldron
        if (state.getBlock() instanceof CauldronBlock) {
            // Fill the cauldron with water
            world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
        }

        // Check if the block can be waterlogged
        if (state.getBlock() instanceof Waterloggable && state.contains(Properties.WATERLOGGED)) {
            // Waterlog the block
            world.setBlockState(pos, state.with(Properties.WATERLOGGED, true), 11);
        }

        if(mode == 0) {
            // Check if the block at the target position can be replaced with water
            if (state.canBucketPlace(Fluids.WATER)) {
                // Set the block to water
                world.setBlockState(pos, Blocks.WATER.getDefaultState(), 11);
            }
        }
    }

    @Override
    protected void handleTick(World world, BlockPos pos) {
        // Loop through positions in a 3x3x3 area around the entity
        for (int x = -charge; x <= charge; x++) {
            for (int y = -charge; y <= charge; y++) {
                for (int z = -charge; z <= charge; z++) {
                    BlockPos targetPos = pos.add(x, y, z);

                    // Check if the block at the target position is lava
                    if (this.world.getFluidState(targetPos).getFluid() == Fluids.LAVA) {
                        // Replace lava with obsidian
                        this.world.setBlockState(targetPos, Blocks.OBSIDIAN.getDefaultState());
                        this.world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        }

        // Check if the entity is above Y 150 and velocity is in up direction
        if (this.getY() > 150 && this.getVelocity().y > 0) {
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) world;

                if (world.isRaining()){
                    serverWorld.setWeather(0, 6000, true, true); // 6000 ticks = 5 minutes of thunderstorm
                } else {
                    serverWorld.setWeather(0, 6000, true, false); // 6000 ticks = 5 minutes of rain
                }

                this.discard();
            }
        }
    }

    protected SoundEvent getEmptyBucketSound() {
        return SoundEvents.ITEM_BUCKET_EMPTY;
    }

    @Override
    protected Item getDefaultItem() {
        // Return the item associated with this entity
        return ModItems.WATER_ORB;
    }
}
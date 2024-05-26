package com.skirlez.fabricatedexchange.item.projectiles;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.skirlez.fabricatedexchange.item.ModEntities;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.item.projectiles.base.FELiquidThrownEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LavaThrownEntity extends FELiquidThrownEntity {

    private int mode;

    public LavaThrownEntity(EntityType<? extends FELiquidThrownEntity> entityType, World world) {
        super(entityType, world);
    }

    public LavaThrownEntity(World world, LivingEntity owner, int charge, int mode) {
        super(ModEntities.LAVA_PROJECTILE, world, owner, Fluids.LAVA, charge);
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
            world.setBlockState(pos, Blocks.LAVA_CAULDRON.getDefaultState());
        }

        // Check if the block can be waterlogged
        if (state.getBlock() instanceof Waterloggable && state.contains(Properties.WATERLOGGED)) {
            // Un-waterlog the block
            world.setBlockState(pos, state.with(Properties.WATERLOGGED, false), 11);
        }


        if(mode == 0) {
            // Check if the block at the target position can be replaced with lava
            if (state.canBucketPlace(Fluids.LAVA)) {
                // Set the block to lava
                world.setBlockState(pos, Blocks.LAVA.getDefaultState(), 11);
            }
        }
    }

    @Override
    protected void handleTick(World world, BlockPos pos) throws CommandSyntaxException {
        // Loop through positions in a 3x3x3 area around the entity
        for (int x = -charge; x <= charge; x++) {
            for (int y = -charge; y <= charge; y++) {
                for (int z = -charge; z <= charge; z++) {
                    BlockPos targetPos = pos.add(x, y, z);

                    // Check if the block at the target position is water
                    if (this.world.getFluidState(targetPos).getFluid() == Fluids.WATER) {
                        BlockState targetBlockState = world.getBlockState(targetPos);
                        if (targetBlockState.getBlock() instanceof Waterloggable && targetBlockState.contains(Properties.WATERLOGGED)) {
                            if (targetBlockState.get(Properties.WATERLOGGED)) {
                                return;
                            }
                        }

                        // Replace water with air
                        this.world.setBlockState(targetPos, Blocks.AIR.getDefaultState());
                        this.world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        }

        // Check if the entity is above Y 150 and velocity is in up direction
        if (this.getY() > 150 && this.getVelocity().y > 0) {
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld) world;
                ServerCommandSource commandSource = serverWorld.getServer().getCommandSource().withWorld(serverWorld);

                CommandDispatcher<ServerCommandSource> dispatcher = serverWorld.getServer().getCommandManager().getDispatcher();
                ParseResults<ServerCommandSource> parseResults = dispatcher.parse("weather clear", commandSource);
                dispatcher.execute(parseResults);
            }

            this.discard();
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity entity = entityHitResult.getEntity();
            entity.setOnFireFor((5 * charge)+ 1);
        }

        if (!this.world.isClient) {
            this.discard();
        }
    }

    protected SoundEvent getEmptyBucketSound() {
        return SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
    }

    @Override
    protected Item getDefaultItem() {
        // Return the item associated with this entity
        return ModItems.LAVA_ORB;
    }
}
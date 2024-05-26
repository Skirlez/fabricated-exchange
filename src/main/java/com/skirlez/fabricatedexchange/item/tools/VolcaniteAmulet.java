package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.projectiles.LavaThrownEntity;
import com.skirlez.fabricatedexchange.item.tools.base.FEAmulet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VolcaniteAmulet extends FEAmulet {
    public VolcaniteAmulet(Settings settings) {
        super(settings, Fluids.LAVA);
    }

    @Override
    public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
        super.doExtraFunction(stack, player);

        if(HadEnoughEMC(stack,player) && !player.getItemCooldownManager().isCoolingDown(this)) {
            World world = player.world;
            Vec3d direction = player.getRotationVec(1.0F);

            int charge = ChargeableItem.getCharge(stack);
            int mode = ItemWithModes.getMode(stack);

            LavaThrownEntity projectile = new LavaThrownEntity(world, player, charge, mode);
            projectile.setVelocity(direction.x, direction.y, direction.z, 2.5F, 0F);

            world.spawnEntity(projectile);
            player.getItemCooldownManager().set(this, 20);
        }
    }

    @Override
    protected boolean handleLiquidSpecificLogic(World world, BlockPos pos, BlockState targetBlockState) {
        // Check if the block is a cauldron
        if (targetBlockState.getBlock() instanceof CauldronBlock) {
            // Fill the cauldron with water
            world.setBlockState(pos, Blocks.LAVA_CAULDRON.getDefaultState());
            return true;
        }

        // Check if the block can be waterlogged
        if (targetBlockState.getBlock() instanceof Waterloggable && targetBlockState.contains(Properties.WATERLOGGED)) {
            // Un-waterlog the block
            world.setBlockState(pos, targetBlockState.with(Properties.WATERLOGGED, false), 11);
            return true;
        }

        return false;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        super.useOnBlock(context);
        context.getWorld().playSound(null, context.getBlockPos(), getEmptyBucketSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
        return ActionResult.PASS;
    }

    protected SoundEvent getEmptyBucketSound() {
        return SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
    }
}
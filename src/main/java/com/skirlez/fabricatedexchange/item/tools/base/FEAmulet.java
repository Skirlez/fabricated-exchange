package com.skirlez.fabricatedexchange.item.tools.base;

import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class FEAmulet extends Item
        implements ExtraFunctionItem, ItemWithModes, ChargeableItem, EmcStoringItem {

    private static final SuperNumber DESIRED_AMOUNT = new SuperNumber(64);

    private int maxCharges = 3;

    protected Fluid liquid;

    public FEAmulet(Settings settings, Fluid liquid) {
        super(settings);
        this.liquid = liquid;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return ChargeableItem.COLOR;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return ChargeableItem.getItemBarStep(stack, getMaxCharge());
    }

    public int getMaxCharge() {
        return maxCharges;
    }

    @Override
    public int getModeAmount() {
        return 2;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Get the block position where the player clicked
        BlockPos pos = context.getBlockPos();

        // Get the world
        World world = context.getWorld();

        // Get the player
        PlayerEntity player = context.getPlayer();

        // Check if the player is allowed to modify the block
        if (player != null && !world.isClient) {
            // Get the block state at the target position
            BlockState targetBlockState = world.getBlockState(pos);

            // Handle specific liquid logic in subclasses
            if (handleLiquidSpecificLogic(world, pos, targetBlockState)) {
                return ActionResult.SUCCESS;
            }

            // Get the face clicked
            Direction face = context.getSide();

            // Adjust the position based on the face clicked
            BlockPos targetPos = pos.offset(face);

            // If the block above the target position can be replaced, place liquid there
            BlockPos offsetPos = targetPos;

            // Get the charge of the item
            int charge = ChargeableItem.getCharge(context.getStack());

            // Calculate the radius of the square (1 for 3x3, 2 for 5x5, etc.)
            int radius = charge;

            ItemStack stack = context.getStack();
            SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);

            // Loop through positions in the square
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    offsetPos = targetPos.add(x, 0, z);
                    BlockState blockState = world.getBlockState(offsetPos);

                    // Check if the block at the target position can be replaced with liquid
                    if (blockState.canBucketPlace(liquid) && storedEmc.compareTo(DESIRED_AMOUNT) >= 0) {

                        // Set the block to liquid
                        storedEmc.subtract(DESIRED_AMOUNT);
                        world.setBlockState(offsetPos, liquid.getDefaultState().getBlockState(), 11);

                        EmcStoringItem.setStoredEmc(stack, storedEmc);

                    }
                }
            }
        }

        context.getPlayer().getItemCooldownManager().set(this, 5);

        return ActionResult.PASS;
    }

    @Override
    public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
        SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);

        int tempAmount = DESIRED_AMOUNT.toInt(0);
        SuperNumber BIG_DESIRED_AMOUNT = new SuperNumber(tempAmount * (ChargeableItem.getCharge(stack)+1));

        if (storedEmc.compareTo(BIG_DESIRED_AMOUNT) < 0) {
            storedEmc = EmcStoringItem.tryConsumeEmc(BIG_DESIRED_AMOUNT, stack, player.getInventory());
        }

        EmcStoringItem.setStoredEmc(stack, storedEmc);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (entity instanceof PlayerEntity player) {
            SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);

            if (storedEmc.compareTo(DESIRED_AMOUNT) < 0) {
                storedEmc = EmcStoringItem.tryConsumeEmc(DESIRED_AMOUNT, stack, player.getInventory());
            }

            EmcStoringItem.setStoredEmc(stack, storedEmc);
        }
    }

    public boolean HadEnoughEMC(ItemStack stack, ServerPlayerEntity player){
        SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);

        int tempAmount = DESIRED_AMOUNT.toInt(0);
        SuperNumber BIG_DESIRED_AMOUNT = new SuperNumber(tempAmount * (ChargeableItem.getCharge(stack)+1));

        if(storedEmc.compareTo(BIG_DESIRED_AMOUNT) < 0) {
            storedEmc = EmcStoringItem.tryConsumeEmc(BIG_DESIRED_AMOUNT, stack, player.getInventory());

            if(storedEmc.compareTo(BIG_DESIRED_AMOUNT) < 0) {
                return false;
            }
        }

        storedEmc.subtract(BIG_DESIRED_AMOUNT);
        EmcStoringItem.setStoredEmc(stack, storedEmc);

        return true;
    }

        protected abstract boolean handleLiquidSpecificLogic(World world, BlockPos pos, BlockState targetBlockState);
}
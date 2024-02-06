package com.skirlez.fabricatedexchange.item.tools;

import java.util.ArrayList;
import java.util.List;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.FakeItemUsageContext;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.OutliningItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class RedMatterAxe extends AxeItem implements ChargeableItem, OutliningItem, ItemWithModes {

    public RedMatterAxe(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getModeAmount() {
        return 2;
    }

    @Override
    public boolean modeSwitchCondition(ItemStack stack) {
        return ChargeableItem.getCharge(stack) != 0;
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

    @Override
    public int getMaxCharge() {
        return 3;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        int charge = ChargeableItem.getCharge(context.getStack());
        if (charge == 0)
            return super.useOnBlock(context);


        boolean anySuccess = false;
        List<BlockPos> positions = getPositionsToOutline(context.getPlayer(), context.getStack(), context.getBlockPos());
        for (BlockPos newPos : positions) {
            FakeItemUsageContext fakeContext = 
                new FakeItemUsageContext(context.getPlayer(), context.getHand(), newPos, Direction.WEST);
            anySuccess = (super.useOnBlock(fakeContext).isAccepted()) || anySuccess;
        }

        return ActionResult.success(anySuccess);
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        float value = super.getMiningSpeedMultiplier(stack, state);
        if (isSuitableFor(state)) {
            value += ChargeableItem.getCharge(stack) * 15; // Increase the multiplier compared to dark matter
        }
        return value;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        // Check if we're on the server side and the block is suitable.
        if (!world.isClient && miner instanceof PlayerEntity && isSuitableFor(state)) {
            PlayerEntity player = (PlayerEntity) miner;

            // Get the positions of all connected logs from the first found suitable block.
            List<BlockPos> positions = getPositionsToOutline(player, stack, pos);

            // Break all the connected logs.
            for (BlockPos newPos : positions) {
                world.breakBlock(newPos, true, player);
            }

        }

        // Call super to handle breaking the initial block and tool damage.
        return super.postMine(stack, world, state, pos, miner);
    }

    private BlockPos findFirstSuitableBlockAround(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction);
            if (isSuitableFor(world.getBlockState(offsetPos))) {
                return offsetPos;
            }
        }
        return null;
    }

    @Override
    public boolean outlineEntryCondition(BlockState state) {
        return Registries.BLOCK.getEntry(state.getBlock()).isIn(BlockTags.LOGS);
    }

    @Override
    public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {
        int mode = ItemWithModes.getMode(stack);
        List<BlockPos> list = new ArrayList<>();

        switch (mode) {
            case 0 -> {
                World world = player.getWorld();
                int range = ChargeableItem.getCharge(stack) * 9; // Adjust range based on charge

                BlockPos firstSuitablePos = findFirstSuitableBlockAround(world, center);
                searchForLogs(world, firstSuitablePos, list, range);
            }
        }
        return list;
    }

    private void searchForLogs(World world, BlockPos pos, List<BlockPos> list, int range) {
        if (pos != null) {
            if (range <= 0 || list.contains(pos)) {
                // Stop if we've reached the maximum range or if the block is already in the list
                return;
            }


            BlockState state = world.getBlockState(pos);
            if (!isSuitableFor(state)) {
                // Stop if the block is not suitable (not a log)
                return;
            }

            list.add(pos); // Add the current log block to the list

            // Recursively search for logs in all six directions
            for (Direction direction : Direction.values()) {
                searchForLogs(world, pos.offset(direction), list, range - 1);
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int charge = ChargeableItem.getCharge(stack);
        if (charge == 0) {
            tooltip.add(Text.translatable("item.fabricated-exchange.mode_switch")
                    .append(" ")
                    .append(Text.translatable("item.fabricated-exchange.red_matter_axe.uncharged")
                            .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));
        }
        else
            ItemWithModes.addModeToTooltip(stack, tooltip);
    }
}

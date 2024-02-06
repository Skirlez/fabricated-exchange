package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.FakeItemUsageContext;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.OutliningItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class RedMatterShovel extends ShovelItem implements ChargeableItem, OutliningItem, ItemWithModes {
    public RedMatterShovel(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
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


        World world = context.getWorld();
        boolean anySuccess = false;
        List<BlockPos> positions = getPositionsToOutline(context.getPlayer(), context.getStack(), context.getBlockPos());
        for (BlockPos newPos : positions) {
            if (world.getBlockState(newPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR)) {
                FakeItemUsageContext fakeContext = 
                    new FakeItemUsageContext(context.getPlayer(), context.getHand(), newPos, Direction.UP);
                anySuccess = (super.useOnBlock(fakeContext).isAccepted()) || anySuccess;
            }
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
        if (isSuitableFor(state) && miner instanceof PlayerEntity player) {
            List<BlockPos> positions = getPositionsToOutline(player, stack, pos);
            for (BlockPos newPos : positions) {
                world.breakBlock(newPos, true, miner);
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public boolean outlineEntryCondition(BlockState state) {
        return isSuitableFor(state);
    }

    @Override
    public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {

        int mode = ItemWithModes.getMode(stack);
        List<BlockPos> list = new ArrayList<BlockPos>();

        switch (mode) {
            case 0 -> {
                int size = ChargeableItem.getCharge(stack) * 2;
                center = center.add(-size, 0, -size);
                int len = size * 2 + 1;
                for (int i = 0; i < len; i++) {
                    for (int j = 0; j < len; j++) {
                        BlockPos newPos = center.add(i, 0, j);
                        if (isSuitableFor(player.getWorld().getBlockState(newPos)))
                            list.add(newPos);
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int charge = ChargeableItem.getCharge(stack);
        if (charge == 0) {
            tooltip.add(Text.translatable("item.fabricated-exchange.mode_switch")
                    .append(" ")
                    .append(Text.translatable("item.fabricated-exchange.red_matter_shovel.uncharged")
                            .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));
        } else {
            ItemWithModes.addModeToTooltip(stack, tooltip);
        }
    }
}

package com.skirlez.fabricatedexchange.item.tools;

import java.util.ArrayList;
import java.util.List;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.OutliningItem;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DarkMatterPickaxe extends PickaxeItem implements ChargeableItem, OutliningItem, ItemWithModes {

    @Override
    public int getModeAmount() {
        return 3;
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
        return 1;
    }
    // Increase mining speed based on charge

    /*
    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        float value = super.getMiningSpeedMultiplier(stack, state);
        if (isSuitableFor(state)) {
            value += ChargeableItem.getCharge(stack) * 12;
        }
        return value;
    }
    */

    public DarkMatterPickaxe(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (isSuitableFor(state) && miner instanceof PlayerEntity player) {
            List<BlockPos> positions = getBlocksToMine(world, stack, miner.getPos(), pos, state);
            for (BlockPos newPos : positions)
                world.breakBlock(newPos, true, miner);
        }
        return super.postMine(stack, world, state, pos, miner);
    }
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int charge = ChargeableItem.getCharge(stack);
        if (charge == 0) {
            tooltip.add(Text.translatable("item.fabricated-exchange.mode_switch")                    
                .append(" ")
                .append(Text.translatable("item.fabricated-exchange.dark_matter_pickaxe.uncharged")
                    .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));
        }
        else
            ItemWithModes.addModeToTooltip(stack, tooltip);
    }




    @Override
    public boolean outlineEntryCondition(BlockState state) {
        return true;
    }

    protected List<BlockPos> getBlocksToMine(World world, ItemStack stack, Vec3d playerPos, BlockPos center, BlockState centerState) {
        List<BlockPos> list = new ArrayList<BlockPos>();
        if (ChargeableItem.getCharge(stack) == 0)
            return list;

        Vec3d relativePos = playerPos.subtract(center.toCenterPos());
        double x = relativePos.getX(), z = relativePos.getZ();
        if (Math.abs(x) > Math.abs(z)) {
            x = 0;
            z = Math.signum(z);
        }
        else {
            z = 0;
            x = Math.signum(x);
        }
        Direction dir = Direction.fromVector((int)x, 0, (int)z);
        assert dir != null;

        int mode = ItemWithModes.getMode(stack);
        switch (mode) {
            // case 0 don't do anything
            case 1 -> dir = Direction.UP;
            case 2 -> dir = dir.rotateYClockwise();
        }



        BlockPos pos1 = center.offset(dir);
        BlockState state1 = world.getBlockState(pos1);
        if (isSuitableFor(state1) && state1.getHardness(null, null) <= (centerState.getHardness(null, null) + 1.5f))
            list.add(pos1);


        BlockPos pos2 = center.offset(dir.getOpposite());
        BlockState state2 = world.getBlockState(pos2);
        if (isSuitableFor(state2) && state2.getHardness(null, null) <= (centerState.getHardness(null, null) + 1.5f))
            list.add(pos2);

        return list;
    }


    @Override
    public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {
        BlockState state = player.getWorld().getBlockState(center);
        return getBlocksToMine(player.getWorld(), stack, player.getPos(), center, state);
    }


}


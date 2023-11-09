package com.skirlez.fabricatedexchange.item.tools;

import java.util.ArrayList;
import java.util.List;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.OutliningItem;

import net.minecraft.block.Block;
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

public class RedMatterPickaxe extends PickaxeItem implements ChargeableItem, OutliningItem, ItemWithModes {

    public RedMatterPickaxe(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getModeAmount() {
        return 5;
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

    // The mining speed could potentially be faster than Dark Matter Pickaxe depending on the design.

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
            List<BlockPos> positions = getBlocksToMine(world, stack, miner.getPos(), pos, state);
            for (BlockPos newPos : positions) {
                world.breakBlock(newPos, true, miner);
            }
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
                    .append(Text.translatable("item.fabricated-exchange.red_matter_pickaxe.uncharged")
                            .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));
        } else {
            ItemWithModes.addModeToTooltip(stack, tooltip);
        }
    }

    @Override
    public boolean outlineEntryCondition(BlockState state) {
        return true;
    }

    protected List<BlockPos> getBlocksToMine(World world, ItemStack stack, Vec3d playerPos, BlockPos center, BlockState centerState) {
        List<BlockPos> list = new ArrayList<BlockPos>();
        if (ChargeableItem.getCharge(stack) == 0)
            return list;



        Vec3d relativePos = playerPos.subtract(Vec3d.of(center));
        double x = relativePos.getX();
        double y = relativePos.getY();
        double z = relativePos.getZ();

        Direction dir;
        if (Math.abs(y) > Math.abs(x) && Math.abs(y) > Math.abs(z)) {
            // Player is looking mostly up or down
            dir = y > 0 ? Direction.UP : Direction.DOWN;
        } else {
            // Player is looking mostly horizontally
            if (Math.abs(x) > Math.abs(z)) {
                x = 0;
                z = Math.signum(z);
            } else {
                z = 0;
                x = Math.signum(x);
            }
            dir = Direction.fromVector((int)x, 0, (int)z);
        }
        assert dir != null;

        int mode = ItemWithModes.getMode(stack);
        switch (mode) {
            case 0 -> {
                addBlockIfSuitable(world, centerState, list, center.offset(dir), center);
                addBlockIfSuitable(world, centerState, list, center.offset(dir.getOpposite()), center);
            }
            case 1 -> {
                dir = Direction.UP;
                addBlockIfSuitable(world, centerState, list, center.offset(dir), center);
                addBlockIfSuitable(world, centerState, list, center.offset(dir.getOpposite()), center);
            }
            case 2 -> {
                if (dir == Direction.UP) {
                    addBlockIfSuitable(world, centerState, list, center.offset(Direction.Axis.Y,-1), center);
                    addBlockIfSuitable(world, centerState, list, center.offset(Direction.Axis.Y,-2), center);
                }
                else if (dir == Direction.DOWN){
                    addBlockIfSuitable(world, centerState, list, center.offset(Direction.Axis.Y,1), center);
                    addBlockIfSuitable(world, centerState, list, center.offset(Direction.Axis.Y,2), center);
                }
                else{
                    dir = dir.rotateYClockwise();
                    addBlockIfSuitable(world, centerState, list, center.offset(dir), center);
                    addBlockIfSuitable(world, centerState, list, center.offset(dir, 2), center); // Adds the second block in the same direction
                }
            }
            case 3 -> {
                int radius = 1; // The radius for the 3x3 area

                for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                    for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                        // Skip the center block itself
                        if (offsetX == 0 && offsetZ == 0) {
                            continue;
                        }

                        BlockPos pos;
                        if (dir == Direction.UP || dir == Direction.DOWN) {
                            // When looking up or down, the x and z offsets are on a horizontal plane
                            for (int offsetY = -radius; offsetY <= radius; offsetY++) {
                                if (offsetY == 0) continue; // Skip the center layer while mining vertically
                                pos = center.add(offsetX, 0, offsetZ);
                                addBlockIfSuitable(world, centerState, list, pos, center);
                            }
                        } else {
                            // When mining horizontally, offsetY is always 0 and the horizontal plane is defined by offsetX and offsetZ
                            if (dir == Direction.SOUTH || dir == Direction.NORTH) {
                                pos = center.add(0, offsetX, offsetZ);
                            } else if (dir == Direction.EAST || dir == Direction.WEST) {
                                pos = center.add(offsetX, offsetZ, 0);
                            } else {
                                continue; // If the direction is not recognized, skip this iteration
                            }
                            addBlockIfSuitable(world, centerState, list, pos, center);
                        }
                    }
                }
            }
            //case 5, do nothing to enable single block
        }

        return list;
    }

    private void addBlockIfSuitable(World world, BlockState centerState, List<BlockPos> list, BlockPos pos, BlockPos center) {
        BlockState state = world.getBlockState(pos);
        // Check if the block at this position is suitable for mining and within the required hardness threshold
        if (isSuitableFor(state) && state.getHardness(world, pos) <= (centerState.getHardness(world, center) + 1.5f)) {
            list.add(pos);
        }
    }

    @Override
    public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {
        BlockState state = player.getWorld().getBlockState(center);
        return getBlocksToMine(player.getWorld(), stack, player.getPos(), center, state);
    }
}
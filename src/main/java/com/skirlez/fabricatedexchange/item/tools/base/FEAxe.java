package com.skirlez.fabricatedexchange.item.tools.base;

import com.skirlez.fabricatedexchange.FabricatedExchange;
import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.FakeItemUsageContext;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.OutliningItem;
import com.skirlez.fabricatedexchange.item.extras.ItemOrb;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class FEAxe extends AxeItem implements ChargeableItem, OutliningItem, ItemWithModes {

    public FEAxe(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public abstract int getModeAmount();

    @Override
    public abstract boolean modeSwitchCondition(ItemStack stack);

    @Override
    public abstract boolean isItemBarVisible(ItemStack stack);

    @Override
    public abstract int getItemBarColor(ItemStack stack);

    @Override
    public abstract int getItemBarStep(ItemStack stack);

    @Override
    public abstract int getMaxCharge();

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        float value = super.getMiningSpeedMultiplier(stack, state);
        if (isSuitableFor(state)) {
            value += ChargeableItem.getCharge(stack) * 15; // Increase the multiplier compared to dark matter
        }
        return value;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        BlockState state = world.getBlockState(pos);

        int mode = ItemWithModes.getMode(stack);

        // Check if we're on the server side, the player is not null, and the block is suitable.
        if (!world.isClient && player != null && isSuitableFor(state)) {
            switch (mode){
                case 0:
                    super.useOnBlock(context);
                    return ActionResult.success(true);
                case 1:
                    List<BlockPos> positions = getPositionsToOutline(player, stack, pos);
                    List<ItemStack> drops = new ArrayList<>();

                    // Collect drops from all connected logs.
                    for (BlockPos newPos : positions) {
                        BlockState newState = world.getBlockState(newPos);
                        if (!newState.isAir()) {
                            LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                                    .random(world.random)
                                    .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(newPos))
                                    .parameter(LootContextParameters.TOOL, stack)
                                    .optionalParameter(LootContextParameters.BLOCK_ENTITY, world.getBlockEntity(newPos));
                            drops.addAll(newState.getDroppedStacks(builder));
                            world.removeBlock(newPos, false); // Remove the block
                        }
                    }

                    // Handle the aggregated drops.
                    if (!drops.isEmpty()) {
                        ItemStack orbStack = ItemOrb.createOrbWithItems(drops); // Assuming createOrbWithItems method exists and works as intended
                        ItemOrb.spawnItemOrb(world, Vec3d.ofCenter(pos), orbStack); // Assuming spawnItemOrb method exists and works as intended
                    }

                    stack.damage(1, player, (p) -> p.sendToolBreakStatus(context.getHand())); // Optionally, apply tool damage outside the loop
                    return ActionResult.SUCCESS;
                case 2:
                    boolean anySuccess = false;
                    positions = getPositionsToOutline(context.getPlayer(), context.getStack(), context.getBlockPos());
                    for (BlockPos newPos : positions) {
                        FakeItemUsageContext fakeContext =
                                new FakeItemUsageContext(context.getPlayer(), context.getHand(), newPos, Direction.WEST);
                        anySuccess = (super.useOnBlock(fakeContext).isAccepted()) || anySuccess;
                    }

                    return ActionResult.success(anySuccess);
                }
            }

        return ActionResult.PASS;
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

        if (mode == 1 || mode == 2){
            World world = player.getWorld();
            int range = ChargeableItem.getCharge(stack) * 9; // Adjust range based on charge

            BlockPos firstSuitablePos = findFirstSuitableBlockAround(world, center);
            searchForLogs(world, firstSuitablePos, list, range);
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

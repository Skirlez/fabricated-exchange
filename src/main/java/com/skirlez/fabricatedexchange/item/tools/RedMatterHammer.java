package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.OutliningItem;
import com.skirlez.fabricatedexchange.item.extras.ItemOrb;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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

public class RedMatterHammer extends PickaxeItem implements ChargeableItem, OutliningItem, ItemWithModes {

    public RedMatterHammer(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getModeAmount() {
        return 4;
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
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int charge = ChargeableItem.getCharge(stack);
        if (charge == 0) {
            tooltip.add(Text.translatable("item.fabricated-exchange.mode_switch")
                    .append(" ")
                    .append(Text.translatable("item.fabricated-exchange.red_matter_hammer.uncharged")
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
        if (ChargeableItem.getCharge(stack) == 0 || !isSuitableFor(centerState))
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
            dir = Direction.fromVector((int) x, 0, (int) z);
        }
        assert dir != null;

        int mode = ItemWithModes.getMode(stack);

        int radius = mode + 1;

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

    private boolean addToPlayerInventoryInReverse(PlayerEntity player, ItemStack itemStack) {
        // Iterate from the last slot of the main inventory to the first slot of the hotbar in reverse
        for (int i = 35; i >= 0; i--) {
            ItemStack existingStack = player.getInventory().getStack(i);
            if (existingStack.isEmpty()) {
                player.getInventory().setStack(i, itemStack.copy());
                itemStack.setCount(0);
                return true; // Successfully added to the inventory
            } else if (ItemStack.canCombine(existingStack, itemStack) && existingStack.getCount() < existingStack.getMaxCount()) {
                int transferAmount = Math.min(existingStack.getMaxCount() - existingStack.getCount(), itemStack.getCount());
                existingStack.increment(transferAmount);
                itemStack.decrement(transferAmount);
                if (itemStack.isEmpty()) {
                    return true; // Successfully added all of itemStack to the inventory
                }
            }
        }
        return itemStack.isEmpty(); // Return true if the itemStack was fully added, false if there's some left
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient()) {
            Vec3d lookVec = player.getRotationVec(1.0F);
            Vec3d rayTraceEnd = player.getCameraPosVec(1.0F).add(lookVec.multiply(5));
            BlockHitResult hitResult = world.raycast(new RaycastContext(player.getCameraPosVec(1.0F), rayTraceEnd, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, player));
            BlockPos pos = hitResult.getBlockPos();

            if (pos != null) {
                BlockState blockState = world.getBlockState(pos);
                if (isSuitableFor(blockState)) { // Check if the block is suitable to be broken
                    List<BlockPos> positionsToMine = getBlocksToMine(world, stack, player.getPos(), pos, blockState);

                    List<ItemStack> drops = new ArrayList<>();
                    for (BlockPos blockPos : positionsToMine) {
                        BlockState state = world.getBlockState(blockPos);
                        if (!state.isAir()) {
                            LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                                    .random(world.random)
                                    .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockPos))
                                    .parameter(LootContextParameters.TOOL, stack)
                                    .optionalParameter(LootContextParameters.BLOCK_ENTITY, world.getBlockEntity(blockPos));
                            drops.addAll(state.getDroppedStacks(builder));
                            world.removeBlock(blockPos, false); // Remove the block
                        }
                    }

                    // Capture drops from the center block
                    if (!blockState.isAir()) {
                        LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                                .random(world.random)
                                .parameter(LootContextParameters.ORIGIN, hitResult.getPos())
                                .parameter(LootContextParameters.TOOL, stack)
                                .optionalParameter(LootContextParameters.BLOCK_ENTITY, world.getBlockEntity(pos));
                        drops.addAll(blockState.getDroppedStacks(builder));
                        world.removeBlock(pos, false); // Remove the center block
                    }

                    // Handle drops
                    if (!drops.isEmpty()) {
                        ItemStack orbStack = ItemOrb.createOrbWithItems(drops);
                        if (!addToPlayerInventoryInReverse(player, orbStack)) {
                            ItemOrb.spawnItemOrb(world, Vec3d.ofCenter(pos), orbStack);
                        }
                    }

                    player.getItemCooldownManager().set(this, 10);
                    stack.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
                }
            }

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.pass(stack);
    }
}
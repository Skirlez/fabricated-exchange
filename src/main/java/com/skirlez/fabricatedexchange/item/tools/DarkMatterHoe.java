package com.skirlez.fabricatedexchange.item.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.FakeItemUsageContext;
import com.skirlez.fabricatedexchange.item.OutliningItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DarkMatterHoe extends HoeItem implements ChargeableItem, OutliningItem {

    public DarkMatterHoe(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
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
        return 2;
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
                new FakeItemUsageContext(context.getPlayer(), context.getHand(), newPos, Direction.UP);
            anySuccess = (super.useOnBlock(fakeContext).isAccepted()) || anySuccess;
        }

        return ActionResult.success(anySuccess);
        
    }

    @Override
    public boolean outlineEntryCondition(BlockState state) {
        return true;
    }

    private final Set<Block> TILLABLE_BLOCKS = TILLING_ACTIONS.keySet();

    @Override
    public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {
        World world = player.getWorld();
        List<BlockPos> list = new ArrayList<BlockPos>();

        int size = ChargeableItem.getCharge(stack);
        center = center.add(-size, 0, -size);
        int len = size * 2 + 1;
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                BlockPos newPos = center.add(i, 0, j);
                if (TILLABLE_BLOCKS.contains(world.getBlockState(newPos).getBlock()) && world.getBlockState(newPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR))
                    list.add(newPos);
            }
        }
        return list;
    }
    
}

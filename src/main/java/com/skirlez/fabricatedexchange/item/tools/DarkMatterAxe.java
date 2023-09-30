package com.skirlez.fabricatedexchange.item.tools;

import java.util.ArrayList;
import java.util.List;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.FakeItemUsageContext;
import com.skirlez.fabricatedexchange.item.OutliningItem;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DarkMatterAxe extends AxeItem implements ChargeableItem, OutliningItem {

	public DarkMatterAxe(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
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
				new FakeItemUsageContext(context.getPlayer(), context.getHand(), newPos, Direction.WEST);
			anySuccess = (super.useOnBlock(fakeContext).isAccepted()) || anySuccess;
		}

		return ActionResult.success(anySuccess);
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
		return Registries.BLOCK.getEntry(state.getBlock()).isIn(BlockTags.LOGS);
	}

	@Override
	public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {
		List<BlockPos> list = new ArrayList<BlockPos>();
		World world = player.getWorld();

		list.add(center);
		int len = ChargeableItem.getCharge(stack) * 3;
		for (int y = 1; y < len; y++) {
			BlockPos newPos = center.add(0, y, 0);
			if (isSuitableFor(world.getBlockState(newPos))) {
				list.add(newPos);
				continue;
			}
			break;
		}
		return list;
	}

	
}

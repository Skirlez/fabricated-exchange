package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;

public class MatterAxe extends AxeItem implements ChargeableItem, OutliningItem, EmcStoringItem {

	private int maxCharges;
	
	public MatterAxe(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
		super(material, attackDamage, attackSpeed, settings);
		if (material == ModToolMaterials.DARK_MATTER_MATERIAL)
			maxCharges = 2;
		else
			maxCharges = 4;
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
		return maxCharges;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		boolean anySuccess = super.useOnBlock(context).isAccepted();
		int charge = ChargeableItem.getCharge(context.getStack());
		if (charge == 0) 
			return ActionResult.success(anySuccess);

		List<BlockPos> positions = getPositionsToOutline(context.getPlayer(), context.getStack(), context.getBlockPos());
		for (BlockPos newPos : positions) {
			FakeItemUsageContext fakeContext = 
				new FakeItemUsageContext(context.getPlayer(), context.getHand(), newPos, Direction.WEST);
			anySuccess = (super.useOnBlock(fakeContext).isAccepted()) || anySuccess;
		}

		return ActionResult.success(anySuccess);
	}

	public static final SuperNumber BLOCK_MINE_COST = new SuperNumber(10);
	
	@Override
	public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
		if (isSuitableFor(state) && miner instanceof PlayerEntity player) {
			List<BlockPos> positions = getPositionsToOutline(player, stack, pos);
			for (BlockPos newPos : positions) {
				if (!EmcStoringItem.takeStoredEmcOrConsume(BLOCK_MINE_COST, stack, player.getInventory())) {
					if (!world.isClient())
						EmcStoringItem.sendNoEmcMessage((ServerPlayerEntity)player);
					break;
				}
				world.breakBlock(newPos, true, miner);
			}
		}
		return super.postMine(stack, world, state, pos, miner);
	}

	
	public static boolean isLog(BlockState state) {
		return Registries.BLOCK.getEntry(state.getBlock()).isIn(BlockTags.LOGS);
	}
	


	public void searchForLogs(World world, BlockPos pos, Set<BlockPos> positions, int depth) {
		if (!positions.contains(pos))
			positions.add(pos);
		if (depth == 0)
			return;
		final BlockPos corner = pos.add(-1, -1, -1);
		GeneralUtil.nestedLoop(3, 3, new Consumer<int[]>() {
			@Override
			public void accept(int[] t) {
				BlockPos currentPos = corner.add(t[0], t[1], t[2]);
				BlockState state = world.getBlockState(currentPos);
				if (isLog(state)) {
					int newDepth = depth - 1;
					searchForLogs(world, currentPos, positions, newDepth);
				}
			}
		});
	}
	
	@Override
	public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos selectedBlockPos) {
		if (!isLog(player.getWorld().getBlockState(selectedBlockPos)))
			return Collections.emptyList();
		Set<BlockPos> positions = new HashSet<BlockPos>();
		World world = player.getWorld();
		int len = ChargeableItem.getCharge(stack);
		searchForLogs(world, selectedBlockPos, positions, len);
		if (positions.contains(selectedBlockPos))
			positions.remove(selectedBlockPos);
		return new ArrayList<BlockPos>(positions);
		/*
		for (int y = 1; y < len; y++) {
			BlockPos newPos = center.add(0, y, 0);
			if (isLog(world.getBlockState(newPos))) {
				list.add(newPos);
				continue;
			}
			break;
		}
		*/
		
	}

}

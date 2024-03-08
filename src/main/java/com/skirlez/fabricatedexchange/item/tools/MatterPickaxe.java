package com.skirlez.fabricatedexchange.item.tools;

import java.util.ArrayList;
import java.util.List;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ItemUtil;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.ModToolMaterials;
import com.skirlez.fabricatedexchange.item.OutliningItem;
import com.skirlez.fabricatedexchange.item.PreMiningItem;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class MatterPickaxe extends PickaxeItem implements ChargeableItem, OutliningItem, ItemWithModes, 
	PreMiningItem, EmcStoringItem {
	
	
	public final int maxCharge;
	
	public MatterPickaxe(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
		super(material, attackDamage, attackSpeed, settings);
		if (material == ModToolMaterials.DARK_MATTER_MATERIAL)
			maxCharge = 1;
		else
			maxCharge = 2;
	}
	@Override
	public int getMaxCharge() {
		return maxCharge;
	}
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



	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		ItemUtil.addModeAndChargeToTooltip(stack, tooltip);
	}


	@Override
	public boolean outlineEntryCondition(BlockState state) {
		return true;
	}

	public static final SuperNumber BLOCK_MINE_COST = new SuperNumber(20);
	
	@Override
	public void preMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
		if (isSuitableFor(state) && miner instanceof PlayerEntity player) {
			List<BlockPos> positions = getBlocksToMine(player, stack, world, pos, state);
			for (BlockPos newPos : positions) {
				if (!EmcStoringItem.takeStoredEmcOrConsume(BLOCK_MINE_COST, stack, player.getInventory())) {
					if (!world.isClient())
						EmcStoringItem.sendNoEmcMessage((ServerPlayerEntity)player);
					break;
				}
				world.breakBlock(newPos, true, miner);
			}
		}
	}


	protected List<BlockPos> getBlocksToMine(PlayerEntity player, ItemStack stack, World world, BlockPos center, BlockState centerState) {
		List<BlockPos> list = new ArrayList<BlockPos>();
		int charge = ChargeableItem.getCharge(stack);
		if (charge == 0)
			return list;
		
		SuperNumber requiredEmc = new SuperNumber(BLOCK_MINE_COST);
		requiredEmc.multiply(charge * 2);
		
		//if (!EmcStoringItem.isAmountConsumable(requiredEmc, stack, player.getInventory()))
		//	return list;
		
		int mode = ItemWithModes.getMode(stack);
		Direction dir = switch (mode) {
			case 0 -> ItemUtil.getHorizontalMineDirection(player, center).rotateYClockwise();
			case 1 -> Direction.UP;
			case 2 -> ItemUtil.getHorizontalMineDirection(player, center);
			
			default -> Direction.UP;
		};
		
		BlockPos pos1 = center;
		BlockPos pos2 = center;
		for (int i = 0; i < charge; i++) {
			pos1 = pos1.offset(dir);
			BlockState state1 = world.getBlockState(pos1);
			if (isSuitableFor(state1) && state1.getHardness(world, pos1) <= (centerState.getHardness(world, center) + 1.5f))
				list.add(pos1);
			
			pos2 = pos2.offset(dir.getOpposite());
			BlockState state2 = world.getBlockState(pos2);
			if (isSuitableFor(state2) && state2.getHardness(world, pos2) <= (centerState.getHardness(world, center) + 1.5f))
				list.add(pos2);
		}

		return list;
	}


	@Override
	public List<BlockPos> getPositionsToOutline(PlayerEntity player, ItemStack stack, BlockPos center) {
		BlockState state = player.getWorld().getBlockState(center);
		return getBlocksToMine(player, stack, player.getWorld(), center, state);
	}
}


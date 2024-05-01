package com.skirlez.fabricatedexchange.item.tools;

import java.util.ArrayList;
import java.util.List;

import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.FakeItemUsageContext;
import com.skirlez.fabricatedexchange.item.ItemUtil;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.ModToolMaterials;
import com.skirlez.fabricatedexchange.item.OutliningItem;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class MatterShovel extends ShovelItem implements ChargeableItem, OutliningItem, EmcStoringItem {
	
	public final int maxCharges;
	
	public MatterShovel(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
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

	public static final SuperNumber BLOCK_MINE_COST = new SuperNumber(8);
	
	@Override
	public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
		if (isSuitableFor(state) && miner instanceof ServerPlayerEntity player) {
			List<BlockPos> positions = getPositionsToOutline(player, stack, pos);
			for (BlockPos newPos : positions) {
				if (!EmcStoringItem.takeStoredEmcOrConsume(BLOCK_MINE_COST, stack, player.getInventory())) {
					EmcStoringItem.sendNoEmcMessage(player);
					break;
				}
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
		List<BlockPos> list = new ArrayList<BlockPos>();
		int size = ChargeableItem.getCharge(stack);
		center = center.add(-size, 0, -size);
		int len = size * 2 + 1;
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len; j++) {
				BlockPos newPos = center.add(i, 0, j);
				if (isSuitableFor(player.getWorld().getBlockState(newPos)))
					list.add(newPos);
			}
		}
		return list;
	}
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		tooltip.add(Text.translatable("item.fabricated-exchange.diggy")
			.setStyle(Style.EMPTY.withColor(Formatting.GOLD)));
		
	}
	
	@Override
	public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
		return super.getMiningSpeedMultiplier(stack, state) * 0.7f;
	}
}

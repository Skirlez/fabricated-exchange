package com.skirlez.fabricatedexchange.item.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.FakeItemUsageContext;
import com.skirlez.fabricatedexchange.item.ModToolMaterials;
import com.skirlez.fabricatedexchange.item.OutliningItem;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class MatterHoe extends HoeItem implements ChargeableItem, OutliningItem, EmcStoringItem {

	int maxCharges;
	
	public MatterHoe(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
		super(material, attackDamage, attackSpeed, settings);
		if (material == ModToolMaterials.DARK_MATTER_MATERIAL)
			maxCharges = 3;
		else
			maxCharges = 6;
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

	/*
	public static final SuperNumber TILL_COST = SuperNumber.One(); // Dirt cheap
	
	// version of useOnBlock that consumes EMC
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		ActionResult initial = super.useOnBlock(context);
		
		
		boolean anySuccess = initial.isAccepted();
		
		int charge = ChargeableItem.getCharge(context.getStack());
		if (charge == 0)
			return ActionResult.success(anySuccess);
		
		World world = context.getWorld();
		List<BlockPos> positions = getPositionsToOutline(context.getPlayer(), context.getStack(), context.getBlockPos());
		
		SuperNumber total = EmcStoringItem.getTotalConsumableEmc(context.getPlayer().getInventory(), context.getStack());
		

		if (total.compareTo(TILL_COST) == -1) {
			if (world.isClient())
				EmcStoringItem.showNoEmcMessage();
			return ActionResult.success(anySuccess);
		}
		SuperNumber debt = SuperNumber.Zero();
		
		
		for (BlockPos newPos : positions) {
			FakeItemUsageContext fakeContext = 
				new FakeItemUsageContext(context.getPlayer(), context.getHand(), newPos, Direction.UP);
				
			boolean success = (super.useOnBlock(fakeContext).isAccepted());
			if (success) {
				total.subtract(TILL_COST);
				debt.add(TILL_COST);
				
				if (total.compareTo(TILL_COST) == -1) {
					if (world.isClient())
						EmcStoringItem.showNoEmcMessage();
					

					EmcStoringItem.takeStoredEmcOrConsume(debt, context.getStack(), context.getPlayer().getInventory());
					return ActionResult.success(anySuccess);
				}
				anySuccess = true;
			}
		}
		EmcStoringItem.takeStoredEmcOrConsume(debt, context.getStack(), context.getPlayer().getInventory());
		
		return ActionResult.success(anySuccess);
		
	}
	*/
	
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
		BlockPos corner = center.add(-size, 0, -size);
		int len = size * 2 + 1;
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < len; j++) {
				BlockPos newPos = corner.add(i, 0, j);
				if (TILLABLE_BLOCKS.contains(world.getBlockState(newPos).getBlock()) && world.getBlockState(newPos.add(0, 1, 0)).getBlock().equals(Blocks.AIR))
					list.add(newPos);
			}
		}
		return list;
	}
	
	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);

	}
	
	
}

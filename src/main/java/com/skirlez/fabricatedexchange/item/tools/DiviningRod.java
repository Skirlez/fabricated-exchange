package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.ItemUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;


public class DiviningRod extends Item {
	private final int distance;

	public DiviningRod(Settings settings, int distance) {
		super(settings);
		this.distance = distance;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		Direction direction = ItemUtil.getMineDirection(context.getPlayer(), context.getBlockPos());

		switch (direction) {
			case NORTH:
				calcEmcInDirection(-1, 1, -1, 1, -distance, 0, context, direction);
				break;
			case SOUTH:
				calcEmcInDirection(-1, 1, -1, 1, 0, distance, context, direction);
				break;
			case WEST:
				calcEmcInDirection(-1, 1, -distance, 0, -1, 1, context, direction);
				break;
			case EAST:
				calcEmcInDirection(-1, 1, 0, distance, -1, 1, context, direction);
				break;
			case UP:
				calcEmcInDirection(0, distance, -1,1, -1, 1, context, direction);
				break;
			case DOWN:
				calcEmcInDirection(-distance, 0, -1,1, -1, 1, context, direction);
				break;
			default:
				break;
		}

		return ActionResult.PASS;
	}


	public void calcEmcInDirection(int dyStart, int dyEnd, int dxStart, int dxEnd, int dzStart, int dzEnd, ItemUsageContext context, Direction direction){
		BlockPos center = context.getBlockPos();
		SuperNumber maxEmc = SuperNumber.ZERO;
		BlockPos maxPos = null;
			
		int blockCount = 0;
		for (int dx = dxStart; dx <= dxEnd; dx++) {
			for (int dy = dyStart; dy <= dyEnd; dy++) {
				for (int dz = dzStart; dz <= dzEnd; dz++) {
					World world = context.getWorld();
					BlockPos pos = center.add(dx, dy, dz);
					Block block = world.getBlockState(pos).getBlock();
					ItemStack blockStack = new ItemStack(block);
					SuperNumber emc = EmcData.getItemStackEmc(blockStack);

					if (emc.equalsZero()) {
						Optional<SmeltingRecipe> smeltingRecipe = world.getRecipeManager().getFirstMatch(RecipeType.SMELTING, new SimpleInventory(new ItemStack[]{blockStack}), world);
						if (smeltingRecipe.isPresent()) {
							DynamicRegistryManager registryManager = world.getRegistryManager();
							emc = EmcData.getItemStackEmc(smeltingRecipe.get().getOutput(registryManager).getItem().getDefaultStack());
						}
					}
					
					if (!emc.equalsZero())
						blockCount++;
						

					if (emc.compareTo(maxEmc) > 0) {
						maxEmc = emc;
						maxPos = pos;
					}
				}
			}
		}

		if (maxPos != null) {
			PlayerEntity player = context.getPlayer();
			if (player != null) {
				Text message = Text.translatable("item.fabricated-exchange.divining_rod.message", 
					maxEmc.toString(), blockCount);
				player.sendMessage(message, true);
			}
		}
	}
}
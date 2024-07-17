package com.skirlez.fabricatedexchange.item.tools.base;

import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class Amulet extends Item
		implements ChargeableItem, EmcStoringItem {

	protected Fluid liquid;
	public Amulet(Settings settings, Fluid liquid) {
		super(settings);
		this.liquid = liquid;
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

	public int getMaxCharge() {
		return 3;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		BlockPos pos = context.getBlockPos();
		World world = context.getWorld();
		PlayerEntity player = context.getPlayer();

		if (player == null)
			return ActionResult.PASS;

		BlockState targetBlockState = world.getBlockState(pos);

		if (handleLiquidSpecificLogic(world, pos, targetBlockState)) {
			return ActionResult.SUCCESS;
		}

		Direction face = context.getSide();
		BlockPos targetPos = pos.offset(face);
		int charge = ChargeableItem.getCharge(context.getStack());
		ItemStack stack = context.getStack();
		boolean success = false;
		for (int x = -charge; x <= charge; x++) {
			for (int z = -charge; z <= charge; z++) {
				BlockPos offsetPos = targetPos.add(x, 0, z);
				BlockState blockState = world.getBlockState(offsetPos);
				if (!blockState.canBucketPlace(liquid))
					continue;
				if (EmcStoringItem.takeStoredEmcOrConsume(getLiquidCost(), stack, player.getInventory())) {
					world.setBlockState(offsetPos, liquid.getDefaultState().getBlockState(), 11);
					success = true;
				}
			}
		}
		if (success) {
			context.getWorld().playSound(null, context.getBlockPos(), getEmptyBucketSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
			context.getPlayer().getItemCooldownManager().set(this, 5);
			return ActionResult.SUCCESS;
		}
		
		return ActionResult.PASS;
	}

	protected abstract boolean handleLiquidSpecificLogic(World world, BlockPos pos, BlockState targetBlockState);
	protected abstract SoundEvent getEmptyBucketSound();
	protected abstract SuperNumber getLiquidCost();
}
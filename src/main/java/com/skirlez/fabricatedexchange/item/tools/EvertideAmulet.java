package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.entities.LavaThrownEntity;
import com.skirlez.fabricatedexchange.entities.WaterThrownEntity;
import com.skirlez.fabricatedexchange.item.ChargeableItem;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.tools.base.Amulet;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import com.skirlez.fabricatedexchange.util.SuperNumber;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EvertideAmulet extends Amulet {
	public EvertideAmulet(Settings settings) {
		super(settings, Fluids.WATER);
	}

	@Override
	protected SuperNumber getLiquidCost() {
		return SuperNumber.Zero();
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (EmcStoringItem.takeStoredEmcOrConsume(getLiquidCost(), stack, player.getInventory()) && !player.getItemCooldownManager().isCoolingDown(this)) {
			Vec3d direction = player.getRotationVec(1.0F);
			int charge = ChargeableItem.getCharge(stack);
			int mode = ItemWithModes.getMode(stack);
			if (!world.isClient()) {
				WaterThrownEntity projectile = new WaterThrownEntity(player, world, charge);

				GeneralUtil.nudgeProjectileInDirection(projectile, direction);
				projectile.setVelocity(direction.x, direction.y, direction.z, 2.5F, 0F);

				world.spawnEntity(projectile);
			}
			player.getItemCooldownManager().set(this, 10);
			return TypedActionResult.success(stack, true);
		}
		return super.use(world, player, hand);
	}

	@Override
	protected boolean handleLiquidSpecificLogic(World world, BlockPos pos, BlockState targetBlockState) {
		if (targetBlockState.getBlock() instanceof CauldronBlock) {
			world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
			return true;
		}
		if (targetBlockState.getBlock() instanceof Waterloggable && targetBlockState.contains(Properties.WATERLOGGED)) {
			world.setBlockState(pos, targetBlockState.with(Properties.WATERLOGGED, true), 11);
			return true;
		}
		return false;
	}

	@Override
	protected SoundEvent getEmptyBucketSound() {
		return SoundEvents.ITEM_BUCKET_EMPTY;
	}
}

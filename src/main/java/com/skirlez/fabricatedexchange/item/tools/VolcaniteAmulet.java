package com.skirlez.fabricatedexchange.item.tools;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.entities.LavaThrownEntity;
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
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class VolcaniteAmulet extends Amulet {
	public VolcaniteAmulet(Settings settings) {
		super(settings, Fluids.LAVA);
	}

	private static final SuperNumber FAILSAFE_LAVA_COST = new SuperNumber(128);
	@Override
	protected SuperNumber getLiquidCost() {
		SuperNumber lavaBucketEmc = EmcData.getItemEmc(Items.LAVA_BUCKET);
		SuperNumber bucketEmc = EmcData.getItemEmc(Items.BUCKET);
		if (lavaBucketEmc.equalsZero() || bucketEmc.equalsZero() || (lavaBucketEmc.compareTo(bucketEmc) <= 0))
			return FAILSAFE_LAVA_COST;
		lavaBucketEmc.subtract(bucketEmc);
		return lavaBucketEmc;
	}


	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

		ItemStack stack = player.getStackInHand(hand);
		if (EmcStoringItem.takeStoredEmcOrConsume(getLiquidCost(), stack, player.getInventory()) && !player.getItemCooldownManager().isCoolingDown(this)) {

			int charge = ChargeableItem.getCharge(stack);
			int mode = ItemWithModes.getMode(stack);
			LavaThrownEntity projectile = new LavaThrownEntity(world, player, charge);
			Vec3d direction = GeneralUtil.getPlayerLookVector(player);
			projectile.setVelocity(direction.x, direction.y, direction.z, 2.5F, 0F);

			projectile.setPosition(player.getX(), player.getEyeY(), player.getZ());

			GeneralUtil.nudgeProjectileInDirection(projectile, direction);

			world.spawnEntity(projectile);
			player.getItemCooldownManager().set(this, 10);
			return TypedActionResult.success(stack, true);
		}
		return super.use(world, player, hand);
	}



	@Override
	protected boolean handleLiquidSpecificLogic(World world, BlockPos pos, BlockState targetBlockState) {
		if (targetBlockState.getBlock() instanceof CauldronBlock) {
			world.setBlockState(pos, Blocks.LAVA_CAULDRON.getDefaultState());
			return true;
		}
		if (targetBlockState.getBlock() instanceof Waterloggable && targetBlockState.contains(Properties.WATERLOGGED)) {
			world.setBlockState(pos, targetBlockState.with(Properties.WATERLOGGED, false), 11);
			return true;
		}
		return false;
	}


	protected SoundEvent getEmptyBucketSound() {
		return SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
	}


}
package com.skirlez.fabricatedexchange.item;

import java.util.List;

import com.skirlez.fabricatedexchange.mixin.ItemAccessor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext.FluidHandling;



public abstract class ItemUtil {
	public static void addModeAndChargeToTooltip(ItemStack stack, List<Text> tooltip) {
		int charge = ChargeableItem.getCharge(stack);
		if (charge == 0) {
			tooltip.add(Text.translatable("item.fabricated-exchange.mode_switch")					
				.append(" ")
				.append(Text.translatable("item.fabricated-exchange.mode_uncharged")
				.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))));
		}
		else
			ItemWithModes.addModeToTooltip(stack, tooltip);
	}

	public static Direction getHorizontalMineDirection(PlayerEntity player, BlockPos pos) {
		BlockHitResult result = ItemAccessor.invokeRaycast(player.getWorld(), player, FluidHandling.NONE);
		Direction dir = result.getSide();
		if (!result.getBlockPos().equals(pos) || dir == Direction.UP || dir.equals(Direction.DOWN))
			return player.getHorizontalFacing().getOpposite();
		return dir;
	}

}

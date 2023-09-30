package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.screen.TransmutationTableScreenHandlerFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TransmutationTablet extends Item {
	public TransmutationTablet(Settings settings) {
		super(settings);
	}
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (!world.isClient()) {
			TransmutationTableScreenHandlerFactory screenHandlerFactory = new TransmutationTableScreenHandlerFactory();
			if (screenHandlerFactory != null) 
				user.openHandledScreen(screenHandlerFactory);
		}
		return TypedActionResult.success(user.getStackInHand(hand), false);
	}

}

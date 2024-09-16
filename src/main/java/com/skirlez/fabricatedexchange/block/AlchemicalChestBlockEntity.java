package com.skirlez.fabricatedexchange.block;

import com.skirlez.fabricatedexchange.item.AlchemicalChestTicker;
import com.skirlez.fabricatedexchange.screen.AlchemicalChestScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlchemicalChestBlockEntity extends BaseChestBlockEntity {
	public AlchemicalChestBlockEntity(BlockPos pos, BlockState state) {
		this(ModBlockEntities.ALCHEMICAL_CHEST, pos, state);
	}

	public AlchemicalChestBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
	}

	@Override
	public int size() {
		return 104;
	}

	@Override
	public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new AlchemicalChestScreenHandler(syncId, playerInventory, this);
	}

	public void tick() {
		progressAnimation();
	}


	public void tickItems(World world, BlockPos pos, BlockState state, BlockEntity entity) {
		Set<Item> itemsToTick = new HashSet<Item>();
		List<ItemStack> stacksToTick = new ArrayList<ItemStack>();

		List<ItemStack> inventory = getInvStackList();
		for (ItemStack stack : inventory) {
			if (!(stack.getItem() instanceof AlchemicalChestTicker))
				continue;
			if (itemsToTick.contains(stack.getItem()))
				continue;
			itemsToTick.add(stack.getItem());
			stacksToTick.add(stack);
		}
		for (ItemStack stack : stacksToTick) {
			AlchemicalChestTicker item = (AlchemicalChestTicker)stack.getItem();
			item.alchemicalChestTick(world, pos, inventory);
		}
	}
}
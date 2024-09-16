package com.skirlez.fabricatedexchange.item;

import com.skirlez.fabricatedexchange.abilities.ItemAbility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class RepairTalisman extends Item implements AbilityGrantingItem, AlchemicalChestTicker {
	public RepairTalisman(Settings settings) {
		super(settings);
	}

	// TODO: Make repairs cost EMC

	// TODO: make only a single repair talisman in inventory effective
	// TODO: work in alchemical chests
	// TODO: work in alchemical bags (once implemented)
	// TODO: work in pedestals (once implemented)

	private static final ItemAbility REPAIR_ABILITY = new ItemAbility() {
		@Override
		public void tick(ItemStack stack, PlayerEntity player) {
			if (player.getWorld().isClient || player.getWorld().getTime() % 20 != 0)
				return;
			PlayerInventory inventory = player.getInventory();
			for (int i = 0; i < inventory.size(); i++) {
				ItemStack itemStack = inventory.getStack(i);
				if (!itemStack.isDamaged())
					continue;
				boolean isMiningWithThisItem = (itemStack == player.getMainHandStack() && player.handSwinging);
				if (!isMiningWithThisItem)
					itemStack.setDamage(itemStack.getDamage() - 1);
			}
		}
		@Override
		public void onRemove(PlayerEntity player) { }
	};
	@Override
	public boolean shouldGrantAbility(PlayerEntity player, ItemStack stack) {
		return true;
	}
	@Override
	public ItemAbility getAbility() {
		return REPAIR_ABILITY;
	}

	@Override
	public void alchemicalChestTick(World world, BlockPos pos, List<ItemStack> inventory) {
		if (world.getTime() % 20 != 0)
			return;
		for (ItemStack stack : inventory) {
			if (!stack.isDamaged())
				continue;
			stack.setDamage(stack.getDamage() - 1);
		}
	}
}

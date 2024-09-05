package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.abilities.ItemAbility;
import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;


public class BlackHoleBand extends Item
		implements ItemWithModes, AbilityGrantingItem {

	public BlackHoleBand(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}

	@Override
	public int getModeAmount() {
		return 2;
	}

	// Helper method to check if the player has at least one empty slot in their inventory
	private boolean hasInventorySpace(PlayerEntity player) {
		for (int i = 0; i < player.getInventory().size(); i++) {
			if (player.getInventory().getStack(i).isEmpty()) {
				return true;
			}
		}
		return false;
	}


	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		int mode = ItemWithModes.getMode(stack);
		tooltip.add(Text.translatable("item.fabricated-exchange.range_switch")
				.append(" ")
				.append(ItemWithModes.getModeName(stack, mode).setStyle(Style.EMPTY.withColor(Formatting.GOLD))));
	}

	private static final ItemAbility ITEM_PULL = new ItemAbility() {
		@Override
		public void tick(ItemStack stack, PlayerEntity player) {
			Box searchBox = GeneralUtil.boxAroundPos(player.getPos(), 5);
			List<ItemEntity> items = player.getWorld().getEntitiesByClass(ItemEntity.class, searchBox, item -> true);
			for (ItemEntity item : items) {
				Vec3d playerOffset = player.getPos().add(0, 1, 0).subtract(item.getPos());
				Vec3d playerDirection = playerOffset.normalize();
				double distance = playerOffset.length();
				item.addVelocity(playerDirection.multiply(1 / Math.max(2.5d, distance)));
			}
		}
		@Override
		public void onRemove(PlayerEntity player) {

		}
	};

	public static boolean isOn(ItemStack stack) {
		return ItemWithModes.getMode(stack) == 1;
	}

	@Override
	public boolean shouldGrantAbility(PlayerEntity player, ItemStack stack) {
		return ItemWithModes.getMode(stack) == 1;
	}

	@Override
	public ItemAbility getAbility() {
		return ITEM_PULL;
	}
}


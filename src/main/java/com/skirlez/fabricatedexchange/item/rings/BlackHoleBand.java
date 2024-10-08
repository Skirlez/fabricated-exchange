package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.abilities.ItemAbility;
import com.skirlez.fabricatedexchange.item.AbilityGrantingItem;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;


public class BlackHoleBand extends Item
		implements AbilityGrantingItem {

	private static final String PULLING_KEY = "FE_Pulling";

	public BlackHoleBand(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
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
		public void onRemove(PlayerEntity player) { }
	};

	public static boolean isOn(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null)
			return false;
		return nbt.getBoolean(PULLING_KEY);
	}

	@Override
	public boolean shouldGrantAbility(PlayerEntity player, ItemStack stack) {
		return isOn(stack);
	}

	@Override
	public ItemAbility getAbility() {
		return ITEM_PULL;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		NbtCompound nbt = user.getStackInHand(hand).getOrCreateNbt();
		nbt.putBoolean(PULLING_KEY, !nbt.getBoolean(PULLING_KEY));
		return super.use(world, user, hand);
	}
}


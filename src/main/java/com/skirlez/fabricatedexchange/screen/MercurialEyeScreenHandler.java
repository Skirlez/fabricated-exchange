package com.skirlez.fabricatedexchange.screen;

import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.screen.slot.DisplaySlot;
import com.skirlez.fabricatedexchange.util.GeneralUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.function.Predicate;

public class MercurialEyeScreenHandler extends ScreenHandler {

	private MercurialEyeInventory eyeInventory;

	public static MercurialEyeScreenHandler clientConstructor(int syncId, PlayerInventory playerInventory) {
		return new MercurialEyeScreenHandler(syncId, playerInventory);
	}

	public MercurialEyeScreenHandler(int syncId, PlayerInventory playerInventory) {
		super(ModScreenHandlers.MERCURIAL_EYE, syncId);

		eyeInventory = new MercurialEyeInventory(playerInventory.player);
		addSlot(new DisplaySlot(eyeInventory, 0, 80, 27));
		GeneralUtil.addPlayerInventory(this, playerInventory, 8, 90);
		GeneralUtil.addPlayerHotbar(this, playerInventory, 8, 148);

	}
	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		super.onSlotClick(slotIndex, button, actionType, player);
		if (slotIndex == 0) {
			DisplaySlot slot = (DisplaySlot)slots.get(0);
			ItemStack cursorStack = getCursorStack();
			if (slot.hasStack() && !cursorStack.isEmpty()
					&& ItemStack.areItemsEqual(slot.getStack(), cursorStack)) {
				slot.setStack(ItemStack.EMPTY);
				return;
			}
			if (cursorStack.isEmpty() || cursorStack.getItem() instanceof BlockItem) {
				cursorStack = cursorStack.copy();
				cursorStack.setCount(1);
				slot.setStack(cursorStack);
			}
		}
	}

	@Override
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return null;
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	private static Item getItemFromMercurialEyeInHand(PlayerEntity player) {
		ItemStack stack = player.getMainHandStack();
		if (stack.getItem() != ModItems.MERCURIAL_EYE) {
			stack = player.getOffHandStack();
			if (stack.getItem() != ModItems.MERCURIAL_EYE)
				return Items.AIR;
		}
		if (!stack.hasNbt())
			return Items.AIR;
		NbtCompound nbt = stack.getNbt();
		Identifier id = new Identifier(nbt.getString("MercurialItemTarget"));

		Item item = Registries.ITEM.get(id);
		return item;
	}

	/** @return the previous stack if removed or an empty stack if failed */
	private static ItemStack setItemInMercurialEyeInHand(PlayerEntity player, Item item) {
		ItemStack stack = player.getMainHandStack();
		if (stack.getItem() != ModItems.MERCURIAL_EYE) {
			stack = player.getOffHandStack();
			if (stack.getItem() != ModItems.MERCURIAL_EYE)
				return new ItemStack(Items.AIR);
		}
		NbtCompound nbt = stack.getOrCreateNbt();
		Identifier id = new Identifier(nbt.getString("MercurialItemTarget"));
		Item previousItem = Registries.ITEM.get(id);

		nbt.putString("MercurialItemTarget", Registries.ITEM.getId(item).toString());
		return new ItemStack(previousItem);
	}

	private static class MercurialEyeInventory implements Inventory {
		private PlayerEntity player;
		public MercurialEyeInventory(PlayerEntity player) {
			this.player = player;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public boolean isEmpty() {
			return getItemFromMercurialEyeInHand(player) != Items.AIR;
		}

		Item lastItem = Items.AIR;
		ItemStack cachedStack = new ItemStack(Items.AIR);

		@Override
		public ItemStack getStack(int slot) {
			Item item = getItemFromMercurialEyeInHand(player);
			if (lastItem != item) {
				cachedStack = new ItemStack(item);
				lastItem = item;
			}
			return cachedStack;
		}

		@Override
		public ItemStack removeStack(int slot, int amount) {
			if (slot == 0 && amount > 0) {
				return setItemInMercurialEyeInHand(player, Items.AIR);
			}
			return new ItemStack(Items.AIR);
		}

		@Override
		public ItemStack removeStack(int slot) {
			if (slot == 0) {
				return setItemInMercurialEyeInHand(player, Items.AIR);
			}
			return new ItemStack(Items.AIR);
		}

		@Override
		public void setStack(int slot, ItemStack stack) {
			if (slot == 0) {
				setItemInMercurialEyeInHand(player, stack.getItem());

			}
		}

		@Override
		public int getMaxCountPerStack() {
			return 1;
		}

		@Override
		public void markDirty() {

		}

		@Override
		public boolean canPlayerUse(PlayerEntity player) {
			return false;
		}

		@Override
		public void onOpen(PlayerEntity player) {
			Inventory.super.onOpen(player);
		}

		@Override
		public void onClose(PlayerEntity player) {
			Inventory.super.onClose(player);
		}

		@Override
		public boolean isValid(int slot, ItemStack stack) {
			return Inventory.super.isValid(slot, stack);
		}

		@Override
		public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
			return Inventory.super.canTransferTo(hopperInventory, slot, stack);
		}

		@Override
		public int count(Item item) {
			return Inventory.super.count(item);
		}

		@Override
		public boolean containsAny(Set<Item> items) {
			return Inventory.super.containsAny(items);
		}

		@Override
		public boolean containsAny(Predicate<ItemStack> predicate) {
			return Inventory.super.containsAny(predicate);
		}

		@Override
		public void clear() {

		}
	}

}

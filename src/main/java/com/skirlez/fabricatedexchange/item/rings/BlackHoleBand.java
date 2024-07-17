package com.skirlez.fabricatedexchange.item.rings;

import com.skirlez.fabricatedexchange.item.*;
import com.skirlez.fabricatedexchange.mixin.ItemAccessor;
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
import net.minecraft.world.World;

import java.util.List;


public class BlackHoleBand extends Item
		implements ExtraFunctionItem, ItemWithModes {

	public boolean active = false;
	public static final String ACTIVE_MODEL_KEY = "CustomModelData";

	public BlackHoleBand(Settings settings) {
		super(settings);
		ItemAccessor self = (ItemAccessor) this;
		self.setRecipeRemainder(this);
	}

	@Override
	public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
		active = !active;
	}

	@Override
	public int getModeAmount() {
		return 10;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);

		if (active) {
			stack.getOrCreateNbt().putInt(ACTIVE_MODEL_KEY, 1);
			if (entity instanceof PlayerEntity player) {
				int range = ItemWithModes.getMode(stack);

				// Define the area around the player to search for items (based on range), starting 1 block above the player
				Box searchBox = new Box(
						player.getX() - range, player.getY() + 1 - range, player.getZ() - range,
						player.getX() + range, player.getY() + 1 + range, player.getZ() + range
				);

				// Find all items within the defined area
				List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, searchBox, item -> true);

				// Pull items towards the player
				for (ItemEntity itemEntity : items) {
					ItemStack itemStack = itemEntity.getStack();

					if (world.isClient) {
						// On the client side, spawn particles
						for (int i = 0; i < 4; i++) {
							double offsetX = (world.random.nextDouble() - 0.5) * 0.1;
							double offsetY = (world.random.nextDouble() - 0.5) * 0.1;
							double offsetZ = (world.random.nextDouble() - 0.5) * 0.1;
							world.addParticle(ParticleTypes.REVERSE_PORTAL, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), offsetX, offsetY, offsetZ);
						}
					} else {
						// Get or create the timer
						if (!itemStack.hasNbt() || !itemStack.getNbt().contains("TeleportTimer")) {
							itemStack.getOrCreateNbt().putInt("TeleportTimer", 10); // Set the initial timer to 20 ticks (1 second)
						} else {
							int teleportTimer = itemStack.getNbt().getInt("TeleportTimer");
							if (teleportTimer > 0) {
								// Decrement the timer
								itemStack.getNbt().putInt("TeleportTimer", teleportTimer - 1);
							} else {
								// Check if the player has space in their inventory
								if (hasInventorySpace(player)) {
									itemStack.removeSubNbt("TeleportTimer"); // Remove the tag when the item is picked up
									if (player.getInventory().insertStack(itemStack)) {
										itemEntity.discard();
									}
								}
							}
						}
					}
				}
			}
		} else {
			stack.getOrCreateNbt().putInt(ACTIVE_MODEL_KEY, 0);
		}
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
	
	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		return false;
	}

}


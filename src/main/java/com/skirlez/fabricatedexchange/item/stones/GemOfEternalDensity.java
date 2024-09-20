package com.skirlez.fabricatedexchange.item.stones;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.EmcStoringItem;
import com.skirlez.fabricatedexchange.item.ItemWithModes;
import com.skirlez.fabricatedexchange.item.ModItems;
import com.skirlez.fabricatedexchange.util.SuperNumber;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.*;


/** The actual behavior of this item, in any variation of this mod, is not documented very well on wikis,
 * so I should write what's implemented for Fabricated Exchange.
 * <p>
 * This item has a target item. Every tick, if it is in the on state, it will consume exactly 1 item from the inventory to accumulate EMC to work towards the target item.
 * The items it can consume are ANY stackable item without NBT (that isn't the target item, of course).
 * <p>
 * It also keeps track of which items it consumed.
 * This list is sorted from lowest EMC value to highest EMC value, so the lowest valued EMC values will be the first
 * that are returned to the player, and the first to be removed once a target item is generated (more on those cases below)
 * <p>
 * If the item has enough stored EMC to make one of the target item, and the player has room for it, it subtracts the target EMC from the stored EMC.
 * Then, it also removes enough items from its list of consumed items to match the EMC of the target item. In total,
 * it's possible that the EMC of the items it removed is higher than the EMC of the target item. Though, this is fine (and unavoidable).
 * The items list only exists as a log of sorts in case you want those items back, not as something to ensure EMC is conserved
 * (that's what the stored EMC value is for)
 * <p>
 * If the item has any items in its list and it is in the off state,
 * it will subtract their values from the stored EMC and give as many of them as it can to the player.
 * It will do so for all of them at the same time, rather than 1 per tick. I
 * (if they don't fit, the gem will keep the items it couldn't give in the list)
 * <p>
 * The case where items have a one EMC value when they are initially consumed,
 * but a higher one when given back due due to player set EMC mappings,
 * resulting in this operation giving this item negative stored EMC, is noted, but
 * not taken care of. I think it actually makes some sense.
 * <p>
 * TODO: Whitelist, blacklist.
 * */
public class GemOfEternalDensity extends Item
		implements ItemWithModes, EmcStoringItem {

	public GemOfEternalDensity(Settings settings) {
		super(settings);
	}

	@Override
	public int getModeAmount() {
		return 4;
	}

	private static Item[] targets = new Item[] { Items.RAW_IRON, Items.RAW_GOLD, Items.DIAMOND, ModItems.DARK_MATTER };
	private static final Item[][] validItemsPerMode = new Item[][]{
			{Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE},
			{Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE, Items.RAW_IRON},
			{Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE, Items.RAW_IRON, Items.RAW_GOLD},
			{Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE, Items.RAW_IRON, Items.RAW_GOLD, Items.DIAMOND}
	};
	private static Item getTargetItem(ItemStack stack) {
		return targets[ItemWithModes.getMode(stack)];
	}


	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		if (world.isClient) {
			return;
		}
		if (!(entity instanceof PlayerEntity player)) {
			return;
		}

		NbtCompound gemNbt = stack.getOrCreateNbt();
		if (isCondensing(stack)) {
			PlayerInventory inventory = player.getInventory();

			Item targetItem = getTargetItem(stack);
			SuperNumber targetEmc = EmcData.getItemEmc(targetItem);
			if (targetEmc.equalsZero()) {
				return;
			}

			// Get the valid items for the current mode
			int currentMode = ItemWithModes.getMode(stack);
			Item[] validItems = validItemsPerMode[currentMode];

			NbtCompound itemsCompound = gemNbt.getCompound("items");

			SuperNumber storedEmc = EmcStoringItem.getStoredEmc(stack);
			String[] keys = getSortedKeyList(itemsCompound);

			if (storedEmc.compareTo(targetEmc) >= 0) {

				if (player.getInventory().insertStack(new ItemStack(targetItem))) {
					SuperNumber targetEmcCopy = new SuperNumber(targetEmc);
					targetEmcCopy.negate();
					EmcStoringItem.addStoredEmc(stack, targetEmcCopy);

					SuperNumber sum = SuperNumber.Zero();

					for (String key : keys) {
						int count = itemsCompound.getInt(key);
						SuperNumber itemEmc = EmcData.getItemEmc(Registries.ITEM.get(new Identifier(key)));

						SuperNumber countRequired = new SuperNumber(targetEmc);
						countRequired.subtract(sum);
						countRequired.divide(itemEmc);
						countRequired.ceil();

						int countTaken = Math.min(countRequired.toInt(1), count);
						itemEmc.multiply(countTaken);
						sum.add(itemEmc);

						assert countTaken <= count;

						if (countTaken == count)
							itemsCompound.remove(key);
						else
							itemsCompound.putInt(key, count - countTaken);
						if (sum.compareTo(targetEmc) >= 0)
							break;
					}
				}

			} else {
				for (int i = 0; i < inventory.size(); i++) {
					ItemStack itemStack = inventory.getStack(i);
					Item item = itemStack.getItem();

					// Check if the item is in the validItems list for the current mode
					if (itemStack.isEmpty()
							|| itemStack.getMaxCount() == 1
							|| itemStack.hasNbt()
							|| item == targetItem
							|| !Arrays.asList(validItems).contains(item)) { // Ensure item is valid for the mode
						continue;
					}

					String itemId = Registries.ITEM.getId(itemStack.getItem()).toString();
					int currentCount = itemsCompound.getInt(itemId);
					EmcStoringItem.addStoredEmc(stack, EmcData.getItemEmc(itemStack.getItem()));
					itemsCompound.putInt(itemId, currentCount + 1);
					inventory.removeStack(i, 1);

					break;
				}
			}

			gemNbt.put("items", itemsCompound);
		} else {
			NbtCompound itemsCompound = gemNbt.getCompound("items");
			String[] keys = getSortedKeyList(itemsCompound);
			for (String key : keys) {
				Item item = Registries.ITEM.get(new Identifier(key));
				SuperNumber baseCost = EmcData.getItemEmc(item);
				int count = itemsCompound.getInt(key);
				int countRemainder = count % item.getMaxCount();
				int countDivision = count / item.getMaxCount();
				boolean canFitMore = true;
				for (int i = 0; i < countDivision && canFitMore; i++) {
					ItemStack itemStack = new ItemStack(item, item.getMaxCount());
					player.getInventory().insertStack(itemStack);
					int newCount = itemStack.getCount();
					int amountGiven = item.getMaxCount() - newCount;
					count -= amountGiven;
					SuperNumber baseCostCopy = new SuperNumber(baseCost);
					baseCostCopy.multiply(amountGiven);
					baseCostCopy.negate();
					EmcStoringItem.addStoredEmc(stack, baseCostCopy);
					if (newCount != 0) {
						canFitMore = false;
					}
				}
				if (canFitMore) {
					ItemStack itemStack = new ItemStack(item, countRemainder);
					player.getInventory().insertStack(itemStack);
					int amountGiven = countRemainder - itemStack.getCount();
					count -= amountGiven;
					SuperNumber baseCostCopy = new SuperNumber(baseCost);
					baseCostCopy.multiply(amountGiven);
					baseCostCopy.negate();
					EmcStoringItem.addStoredEmc(stack, baseCostCopy);
				}
				if (count == 0) {
					itemsCompound.remove(key);
				} else {
					itemsCompound.putInt(key, count);
				}
			}

			gemNbt.put("items", itemsCompound);
		}
	}

	private static SuperNumber getInternalItemEmc(String key, NbtCompound itemsCompound) {
		int count = itemsCompound.getInt(key);
		SuperNumber stackEMC = EmcData.getItemEmc(Registries.ITEM.get(new Identifier(key)));
		stackEMC.multiply(count);
		return stackEMC;
	}
	private static String[] getSortedKeyList(NbtCompound itemsCompound) {
		String[] keys = itemsCompound.getKeys().toArray(new String[0]); // very stupid that it requires you to create an empty array here
		Arrays.sort(keys, (key1, key2) -> {
			SuperNumber emc1 = getInternalItemEmc(key1, itemsCompound);
			SuperNumber emc2 = getInternalItemEmc(key2, itemsCompound);
			return emc1.compareTo(emc2);
		});
		return keys;
	}

	public static boolean isCondensing(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		if (nbt == null)
			return false;
		return nbt.getBoolean("condensing");
	}


	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		NbtCompound nbt = user.getStackInHand(hand).getOrCreateNbt();
		nbt.putBoolean("condensing", !nbt.getBoolean("condensing"));
		return super.use(world, user, hand);
	}
}
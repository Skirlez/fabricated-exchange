package com.skirlez.fabricatedexchange.item.stones;

import com.skirlez.fabricatedexchange.emc.EmcData;
import com.skirlez.fabricatedexchange.item.ExtraFunctionItem;
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
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GemOfEternalDensity extends Item
        implements ExtraFunctionItem, ItemWithModes {

    public boolean active = false;
    public static final String ACTIVE_MODEL_KEY = "CustomModelData";

    private static final Item[][] validItemsPerMode = new Item[][]{
            {Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE},
            {Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE, Items.RAW_IRON},
            {Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE, Items.RAW_IRON, Items.RAW_GOLD},
            {Items.DIORITE, Items.ANDESITE, Items.GRANITE, Items.TUFF, Items.COBBLESTONE, Items.STONE, Items.COBBLED_DEEPSLATE, Items.DEEPSLATE, Items.RAW_IRON, Items.RAW_GOLD, Items.DIAMOND}
    };    private static final Map<Item, Item> itemMapper = new HashMap<>();

    static {
        itemMapper.put(Items.COBBLESTONE, Items.RAW_IRON);
        itemMapper.put(Items.STONE, Items.RAW_IRON);
        itemMapper.put(Items.DEEPSLATE, Items.RAW_IRON);
        itemMapper.put(Items.COBBLED_DEEPSLATE, Items.RAW_IRON);
        itemMapper.put(Items.GRANITE, Items.RAW_IRON);
        itemMapper.put(Items.ANDESITE, Items.RAW_IRON);
        itemMapper.put(Items.DIORITE, Items.RAW_IRON);
        itemMapper.put(Items.TUFF, Items.RAW_IRON);
        itemMapper.put(Items.RAW_IRON, Items.RAW_GOLD);
        itemMapper.put(Items.RAW_GOLD, Items.DIAMOND);
        itemMapper.put(Items.DIAMOND, ModItems.DARK_MATTER);
    }

    public GemOfEternalDensity(Settings settings) {
        super(settings);
    }

    @Override
    public void doExtraFunction(ItemStack stack, ServerPlayerEntity player) {
        active = !active;
    }

    @Override
    public int getModeAmount() {
        return 4;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (active) {
            stack.getOrCreateNbt().putInt(ACTIVE_MODEL_KEY, 1);
            if (entity instanceof PlayerEntity player) {
                PlayerInventory inventory = player.getInventory();

                // Get the current NBT data of the gem
                NbtCompound gemNbt = stack.getNbt();
                if (gemNbt == null) {
                    gemNbt = new NbtCompound();
                }

                int mode = gemNbt.getInt("FE_Mode");
                Item[] validItems = validItemsPerMode[mode];

                // Iterate over the inventory
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack itemStack = inventory.getStack(i);

                    if (Arrays.asList(validItems).contains(itemStack.getItem())) {
                        String itemKey = itemStack.getItem().toString();

                        // Get the current count of the item in the gem's NBT data
                        int currentCount = gemNbt.contains(itemKey, NbtElement.INT_TYPE) ? gemNbt.getInt(itemKey) : 0;

                        // Add the count of the item in the inventory to the gem's NBT data
                        gemNbt.putInt(itemKey, currentCount + itemStack.getCount());

                        // Remove the item from the inventory
                        inventory.removeStack(i);
                    }
                }

                // Iterate over the NBT data of the gem
                for (String key : gemNbt.getKeys()) {

                    if ("CustomModelData".equals(key) || "FE_Mode".equals(key)){
                        continue;
                    }

                    Item currentItem = Registries.ITEM.get(new Identifier(key));
                    Item mappedItem = itemMapper.get(currentItem);
                    int currentCount = gemNbt.getInt(key);

                    SuperNumber currentItemEMC = EmcData.getItemEmc(currentItem);
                    SuperNumber multiplierItemEMC = EmcData.getItemEmc(currentItem);
                    SuperNumber mappedItemEMC = EmcData.getItemEmc(mappedItem);

                    multiplierItemEMC.multiply(currentCount);

                    int totalEMC = multiplierItemEMC.toInt(0);
                    int neededEMC = mappedItemEMC.toInt(0);
                    int originalEMC = currentItemEMC.toInt(0);

                    if (totalEMC == 0 || neededEMC == 0 || originalEMC == 0) {
                        continue;
                    }

                    if (totalEMC >= neededEMC) {
                        int conversionCount = totalEMC / neededEMC;
                        int remainingEMC = totalEMC % neededEMC;
                        int remainingCount = remainingEMC / originalEMC;

                        gemNbt.putInt(key, remainingCount);
                        player.giveItemStack(new ItemStack(mappedItem, conversionCount));
                    }
                }

                // Update the NBT data of the gem
                stack.setNbt(gemNbt);
            }
        } else {
            stack.getOrCreateNbt().putInt(ACTIVE_MODEL_KEY, 0);

            if (entity instanceof PlayerEntity player) {
                NbtCompound gemNbt = stack.getNbt();
                if (gemNbt == null) {
                    gemNbt = new NbtCompound();
                }

                NbtCompound iteratorGemNbt = gemNbt.copy();

                // Iterate over the NBT data of the gem
                for (String key : iteratorGemNbt.getKeys()) {
                    if ("CustomModelData".equals(key) || "FE_Mode".equals(key)) {
                        continue;
                    }

                    // Get the current count of the item in the gem's NBT data
                    int currentCount = iteratorGemNbt.getInt(key);

                    // Create a new ItemStack for the item and give it to the player
                    Item currentItem = Registries.ITEM.get(new Identifier(key));
                    player.giveItemStack(new ItemStack(currentItem, currentCount));

                    // Remove the key from the gem's NBT data
                    gemNbt.remove(key);
                }

                // Update the NBT data of the gem
                stack.setNbt(gemNbt);
            }
        }
    }
}
package com.skirlez.fabricatedexchange.item.extras;

import com.skirlez.fabricatedexchange.item.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class ItemOrb extends Item {

    public ItemOrb(Settings settings) {
        super(settings);
    }

    // Method to create an ItemOrb ItemStack with a list of ItemStacks stored in its NBT.
    public static ItemStack createOrbWithItems(List<ItemStack> items) {
        ItemStack orbStack = new ItemStack(ModItems.ITEM_ORB);
        NbtCompound nbt = new NbtCompound();
        NbtList listTag = new NbtList();

        for (ItemStack item : items) {
            NbtCompound itemNbt = new NbtCompound();
            item.writeNbt(itemNbt);
            listTag.add(itemNbt);
        }

        nbt.put("StoredItems", listTag);
        orbStack.setNbt(nbt);

        return orbStack;
    }

    // Method to spawn the orb entity at the given position in the world.
    public static void spawnItemOrb(World world, Vec3d pos, ItemStack orbStack) {
        ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), orbStack);
        world.spawnEntity(itemEntity);
    }

    // Example usage method that could be called when the player uses the ItemOrb.
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack orbStack = player.getStackInHand(hand);
        if (!world.isClient) {
            releaseStoredItems(orbStack, player, world);
            orbStack.decrement(1); // Remove the orb from the player's inventory after use.
        }
        return TypedActionResult.success(orbStack, world.isClient());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (entity instanceof PlayerEntity player && !world.isClient) {
            stack.decrement(1); // Remove the orb from the player's inventory after use.
            releaseStoredItems(stack, player, world);
        }
    }

    // Method to "release" the stored items, for example, adding them to the player's inventory.
    private void releaseStoredItems(ItemStack orbStack, PlayerEntity player, World world) {
        NbtCompound nbt = orbStack.getNbt();
        if (nbt != null && nbt.contains("StoredItems", 9)) { // 9 is the NBT tag type for ListTag.
            NbtList listTag = nbt.getList("StoredItems", 10); // 10 is the NBT tag type for CompoundTag.

            for (int i = 0; i < listTag.size(); i++) {
                ItemStack item = ItemStack.fromNbt(listTag.getCompound(i));
                if (!player.giveItemStack(item)) {
                    // If the player's inventory is full, drop the item in the world.
                    player.dropItem(item, false);
                }
            }
        }
    }
}

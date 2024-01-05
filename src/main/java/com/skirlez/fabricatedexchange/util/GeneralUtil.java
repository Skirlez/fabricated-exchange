package com.skirlez.fabricatedexchange.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.skirlez.fabricatedexchange.mixin.ScreenHandlerInvoker;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.RegistryEntryList.Named;
import net.minecraft.world.World;

public class GeneralUtil {
    public static <T> void mergeMap(Map<String, T> map, Map<String, T> newMap) {
        int iterations = newMap.keySet().size();
        Iterator<String> iterator = newMap.keySet().iterator();
        for (int i = 0; i < iterations; i++) {
            String s = iterator.next();
            map.put(s, newMap.get(s));
        }
    }


    public static List<BlockEntity> getNeighboringBlockEntities(World world, BlockPos pos) {
        ArrayList<BlockEntity> list = new ArrayList<BlockEntity>(6);
        for (int i = 0; i < 6; i++) {
            int xOffset = 0, yOffset = 0, zOffset = 0;
            switch (i) {
                case 0:
                    xOffset = 1;
                    break;
                case 1:
                    xOffset = -1;
                    break;
                case 2:
                    zOffset = 1;
                    break;
                case 3:
                    zOffset = -1;
                    break;
                case 4:
                    yOffset = 1;
                    break;
                case 5:
                    yOffset = -1;
                    break;
            }
            BlockEntity blockEntity = world.getBlockEntity(pos.add(xOffset, yOffset, zOffset));
            if (blockEntity != null)
                list.add(blockEntity);
        }
        list.trimToSize();
        return list;
    }


    public static void addPlayerInventory(ScreenHandler self, PlayerInventory playerInventory, int x, int y) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++)
                ((ScreenHandlerInvoker)self).invokeAddSlot(new Slot(playerInventory, j + i * 9 + 9, x + j * 18, y + i * 18));
        }
    }

    public static void addPlayerHotbar(ScreenHandler self, PlayerInventory playerInventory, int x, int y) {
        for (int i = 0; i < 9; i++)
            ((ScreenHandlerInvoker)self).invokeAddSlot(new Slot(playerInventory, i, x + i * 18, y));
    }
    
    public static Box boxAroundPos(Vec3d pos, double size) {
        return new Box(
            pos.getX() - size, pos.getY() - size, pos.getZ() - size,
            pos.getX() + size, pos.getY() + size, pos.getZ() + size);
    }

    @Nullable
    public static Item getAnyItemFromItemTag(TagKey<Item> tag) {
        Optional<Named<Item>> optionalNamed = Registry.ITEM.getEntryList(tag);
        if (optionalNamed.isEmpty())
            return null;
        Named<Item> named = optionalNamed.get();
        if (named.size() == 0)
            return null;
        return named.get(0).value();
    } 
    
    private static Item[] emptyArr = new Item[0];


    public static <T extends ItemConvertible> Item[] getItemsFromTag(TagKey<T> tag, DefaultedRegistry<T> registry) {
        Optional<Named<T>> optionalNamed = registry.getEntryList(tag);
        if (optionalNamed.isEmpty())
            return emptyArr;
        Named<T> named = optionalNamed.get();
        Item[] array = new Item[named.size()];
        for (int i = 0; i < named.size(); i++)
            array[i] = named.get(i).value().asItem();
        return array;
    } 
    public static Item[] getItemsFromTagString(String tagString) {
        Identifier tagId = new Identifier(tagString);

        TagKey<Item> tag = Registry.ITEM.streamTags().filter((key) -> key.id().equals(tagId)).findFirst().orElse(null);
        if (tag != null)
            return getItemsFromTag(tag, Registry.ITEM);

        TagKey<Block> blockTag = Registry.BLOCK.streamTags().filter((key) -> key.id().equals(tagId)).findFirst().orElse(null);
        return getItemsFromTag(blockTag, Registry.BLOCK);
        
    } 
    public static String[] getItemStringsFromTagString(String tagString) {
        Item[] items = getItemsFromTagString(tagString);
        String[] array = new String[items.length];
        for (int i = 0; i < items.length; i++)
            array[i] = Registry.ITEM.getId(items[i]).toString();
        
        return array;
    }
}

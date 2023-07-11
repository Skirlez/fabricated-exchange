package com.skirlez.fabricatedexchange.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.skirlez.fabricatedexchange.emc.EmcData;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
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
    public static void addSortedEmcList(List<Item> list, Item item) {
        if (list.size() == 0) {
            list.add(item);
            return;
        }
        SuperNumber num = EmcData.getItemEmc(item);
        int low = 0;
        int high = list.size() - 1;
        
        while (low <= high) {
            int mid = (low + high) / 2;
            SuperNumber midNum = EmcData.getItemEmc(list.get(mid));
            
            if (num.compareTo(midNum) == -1) {
                high = mid - 1;
            } 
            else if (num.compareTo(midNum) == 1) {
                low = mid + 1;
            } 
            else {
                list.add(mid + 1, item);
                return;
            }
        }
        
        list.add(low, item);
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
}

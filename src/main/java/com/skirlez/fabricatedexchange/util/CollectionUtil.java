package com.skirlez.fabricatedexchange.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.skirlez.fabricatedexchange.emc.EmcData;

import net.minecraft.item.Item;

public class CollectionUtil {
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

    public static int searchSortedEmcList(List<Item> list, Item item) {
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
                return mid;
            }
        }

        return -1;
    }

}

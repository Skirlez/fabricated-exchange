package com.skirlez.fabricatedexchange.util;

import java.util.Iterator;
import java.util.Map;

public class MapUtil {
    public static <T> void mergeMap(Map<String, T> map, Map<String, T> newMap) {
        int iterations = newMap.keySet().size();
        Iterator<String> iterator = newMap.keySet().iterator();
        for (int i = 0; i < iterations; i++) {
            String s = iterator.next();
            map.put(s, newMap.get(s));
        }
    }
}

package com.skirlez.fabricatedexchange.util.config;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Iterator;
import com.skirlez.fabricatedexchange.util.DataFile;
import com.skirlez.fabricatedexchange.util.GeneralUtil;

public class ModifiersFile extends DataFile<HashSet<String>> {

    public ModifiersFile(Type type, String name) {
        super(type, name);
        tagModifiers = new HashSet<String>();
    }

    private HashSet<String> tagModifiers;

    @Override
    protected void process() {
        tagModifiers.clear();
        Iterator<String> iterator = value.iterator();
        while (iterator.hasNext()) {
            String entry = iterator.next();
            if (!entry.startsWith("#"))
                continue;
            entry = entry.substring(1);
            String[] items = GeneralUtil.getItemStringsFromTagString(entry);
            for (int i = 0; i < items.length; i++) {
                tagModifiers.add(items[i]);
            }
        }
    }

    public boolean hasItem(String item) {
        return value.contains(item) || tagModifiers.contains(item);
    }
}
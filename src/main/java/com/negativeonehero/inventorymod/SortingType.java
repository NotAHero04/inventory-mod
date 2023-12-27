package com.negativeonehero.inventorymod;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public enum SortingType {
    COUNT(Comparator.comparingInt(ItemStack::getCount).thenComparing(stack -> stack.getName().getString()),
            "Sort by Count"),
    NAME(Comparator.comparing(stack -> stack.getName().getString()),
            "Sort by Name"),
    ID(Comparator.comparingInt((ItemStack i) -> Item.getRawId(i.getItem()))
            .thenComparing(stack -> stack.getName().getString()),
            "Sort by ID");
    private final Comparator<ItemStack> comparator;
    public static final SortingType[] types = values();
    public final String message;

    SortingType(Comparator<ItemStack> comparator, String message) {
        this.comparator = comparator;
        this.message = message;
    }

    public void sort(ArrayList<ItemStack> list, boolean ascending) {
        Comparator<ItemStack> comparator = this.comparator;
        if (comparator != null)
            list.sort(comparator);
        if(!ascending)
            Collections.reverse(list);
    }

    public SortingType next() {
        return types[(this.ordinal() + 1) % 3];
    }
}

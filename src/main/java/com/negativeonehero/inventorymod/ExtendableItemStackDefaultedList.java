package com.negativeonehero.inventorymod;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class ExtendableItemStackDefaultedList extends DefaultedList<ItemStack> {
    public final ArrayList<ItemStack> delegate;
    public @Nullable ItemStack initialElement;
    public ExtendableItemStackDefaultedList(ArrayList<ItemStack> delegate, @Nullable ItemStack initialElement) {
        super(delegate, initialElement);
        this.delegate = delegate;
        this.initialElement = initialElement;
    }

    public static ExtendableItemStackDefaultedList ofSize(int size, ItemStack defaultValue) {
        Objects.requireNonNull(defaultValue);
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i=0; i<size; ++i) list.add(defaultValue);
        return new ExtendableItemStackDefaultedList(list, defaultValue);
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public ItemStack set(int index, ItemStack stack) {
        Objects.requireNonNull(stack);
        return this.delegate.set(index, stack);
    }

    @Override
    public boolean add(ItemStack stack) {
        Objects.requireNonNull(stack);
        this.delegate.add(stack);
        return false;
    }

    public void removeRange(int startIndex, int endIndex) {
        this.delegate.subList(startIndex, endIndex).clear();
    }

    @Override
    public @NotNull ItemStack get(int index) {
        return this.delegate.get(index);
    }
}

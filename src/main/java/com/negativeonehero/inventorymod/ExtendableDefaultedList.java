package com.negativeonehero.inventorymod;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class ExtendableDefaultedList extends DefaultedList<ItemStack> {
    public final ArrayList<ItemStack> delegate;
    public @Nullable ItemStack initialElement;
    protected ExtendableDefaultedList(ArrayList<ItemStack> delegate, @Nullable ItemStack initialElement) {
        super(delegate, initialElement);
        this.delegate = delegate;
        this.initialElement = initialElement;
    }

    public static ExtendableDefaultedList ofSize(int size, ItemStack defaultValue) {
        Objects.requireNonNull(defaultValue);
        ArrayList<ItemStack> list = Lists.newArrayList();
        for (int i=0; i<size; ++i) list.add(defaultValue);
        return new ExtendableDefaultedList(list, defaultValue);
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

    @Override
    public @NotNull ItemStack get(int index) {
        return this.delegate.get(index);
    }
}

package com.negativeonehero.inventorymod;

import net.minecraft.util.collection.DefaultedList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ExtendableDefaultedList<E> extends DefaultedList<E> {
    private ArrayList<E> delegate;
    private @Nullable E initialElement;

    protected ExtendableDefaultedList(List<E> delegate, @Nullable E initialElement) {
        super(delegate, initialElement);
        this.delegate = new ArrayList<>(delegate);
        this.initialElement = initialElement;
    }

    public static <E> ExtendableDefaultedList<E> ofSize(int size, E defaultValue) {
        Objects.requireNonNull(defaultValue);
        Object[] objects = new Object[size];
        Arrays.fill(objects, defaultValue);
        return new ExtendableDefaultedList(Arrays.asList(objects), defaultValue);
    }

    public boolean add(E element) {
        Objects.requireNonNull(element);
        this.delegate.add(element);
        return true;
    }
}

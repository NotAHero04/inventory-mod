package com.negativeonehero.inventorymod.impl;

import com.negativeonehero.inventorymod.ExtendableItemStackDefaultedList;
import com.negativeonehero.inventorymod.SortingType;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface IPlayerInventory {
    void setMainExtras(ExtendableItemStackDefaultedList mainExtras1);
    ExtendableItemStackDefaultedList getMainExtras();
    void setContentChanged();
    int getLastEmptySlot();
    List<ItemStack> getCombinedMainNoHotbar();
    ItemStack removeExtrasStack(int slot);
    void swapInventory(int page);
    void sort(boolean ascending, int page, SortingType sortingType);
    void needsToSync();
}

package com.negativeonehero.inventorymod.impl;

import com.negativeonehero.inventorymod.ExtendableItemStackDefaultedList;
import net.minecraft.item.ItemStack;

public interface IPlayerInventory {
    void setMainExtras(ExtendableItemStackDefaultedList mainExtras1);
    ExtendableItemStackDefaultedList getMainExtras();
    void setContentChanged();
    int getLastEmptySlot();
    ItemStack removeExtrasStack(int slot);
}

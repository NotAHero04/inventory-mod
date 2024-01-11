package com.negativeonehero.inventorymod.impl;

import com.negativeonehero.inventorymod.utils.ExtendableItemStackDefaultedList;
import com.negativeonehero.inventorymod.utils.SortingType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public interface IPlayerInventory {
    ExtendableItemStackDefaultedList getMainExtras();
    void setContentChanged();
    List<ItemStack> getCombinedMainNoHotbar();
    ItemStack removeExtrasStack(int slot);
    void sort(boolean ascending, SortingType sortingType);
    void needsToSync();
    void setServerPlayer(ServerPlayerEntity player);
    ServerPlayerEntity getServerPlayer();
}

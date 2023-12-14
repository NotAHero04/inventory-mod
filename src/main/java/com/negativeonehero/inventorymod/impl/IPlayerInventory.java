package com.negativeonehero.inventorymod.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface IPlayerInventory {
    public DefaultedList<ItemStack> getExtraInventory();
}

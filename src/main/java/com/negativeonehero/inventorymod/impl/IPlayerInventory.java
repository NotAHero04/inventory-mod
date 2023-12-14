package com.negativeonehero.inventorymod.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;

public interface IPlayerInventory {
    public ArrayList<ItemStack> getExtraInventory();
}

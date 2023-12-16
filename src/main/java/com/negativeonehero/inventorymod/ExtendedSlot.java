package com.negativeonehero.inventorymod;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public class ExtendedSlot extends Slot {

    private boolean isVisible;
    public ExtendedSlot(Inventory inventory, int index, int x, int y, boolean isVisible) {
        super(inventory, index, x, y);
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}

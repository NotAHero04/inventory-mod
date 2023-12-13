package com.negativeonehero.inventorymod.mixin;

import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Inventory.class)
public interface InventoryMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite
    default int getMaxCountPerStack() {
        return Integer.MAX_VALUE;
    }
}

package com.negativeonehero.inventorymod.mixin;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.class)
public class ItemMixin {

    @Shadow
    private int maxCount;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public final int getMaxCount() {
        return Integer.MAX_VALUE;
    }
}

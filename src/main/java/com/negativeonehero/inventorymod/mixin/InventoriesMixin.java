package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Inventories.class)
public class InventoriesMixin {
    @Inject(method = "remove(Lnet/minecraft/inventory/Inventory;Ljava/util/function/Predicate;IZ)I", at = @At("TAIL"))
    private static void sync(Inventory inventory, Predicate<ItemStack> shouldRemove, int maxCount, boolean dryRun, CallbackInfoReturnable<Integer> cir) {
        if (inventory instanceof IPlayerInventory) ((IPlayerInventory) inventory).needsToSync();
    }
}

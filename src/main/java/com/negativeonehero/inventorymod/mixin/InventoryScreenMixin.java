package com.negativeonehero.inventorymod.mixin;

import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(method = "isClickOutsideBounds(DDIII)Z", at = @At(value = "RETURN"), cancellable = true)
    protected void isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> ci) {
        boolean oldClickOutsideBounds = ci.getReturnValue();
        boolean clickOutsideButtons = mouseX < 10 || mouseX > 102 || mouseY < 10 || mouseY > 26;
        if (!oldClickOutsideBounds || !clickOutsideButtons) ci.setReturnValue(false);
    }
}

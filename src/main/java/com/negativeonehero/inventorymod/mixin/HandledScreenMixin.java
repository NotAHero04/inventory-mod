package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.ExtendedSlot;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    @Unique
    private PlayerInventory inventory;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.inventory = inventory;
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void init(CallbackInfo ci) {

    }

    @Inject(method = "drawSlot", at = @At(value = "HEAD"), cancellable = true)
    private void drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (slot instanceof ExtendedSlot && !(((ExtendedSlot)slot).isVisible()))
            ci.cancel();
    }
}

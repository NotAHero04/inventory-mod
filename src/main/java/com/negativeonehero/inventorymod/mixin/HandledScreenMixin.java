package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements IPlayerInventory {

    @Unique
    private IPlayerInventory inventory;
    @Unique
    private ArrayList<ItemStack> mainExtras;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        if(inventory instanceof IPlayerInventory) {
            this.inventory = (IPlayerInventory) inventory;
        }
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void init(CallbackInfo ci) {
        this.mainExtras = this.inventory.getExtraInventory();
        // TODO: Add some buttons
    }
}

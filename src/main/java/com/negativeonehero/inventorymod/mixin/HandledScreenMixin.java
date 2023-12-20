package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    @Shadow @Final
    protected T handler;
    @Shadow
    protected int backgroundWidth;
    @Shadow
    protected int backgroundHeight;
    @Unique
    private PlayerInventory inventory;

    @Unique
    private ButtonWidget previousButton;
    @Unique
    private ButtonWidget nextButton;
    @Unique
    private ButtonWidget functionButton;
    @Unique
    private int page = 1;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.inventory = inventory;
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void init(CallbackInfo ci) {
        this.previousButton = ButtonWidget.builder(Text.of("<"), button -> {
            this.updateButtons(false);
        })
                .position(10, 10)
                .size(16, 16).tooltip(Tooltip.of(Text.of("Page " + (page - 1))))
                .build();
        this.addDrawableChild(this.previousButton);
        this.nextButton = ButtonWidget.builder(Text.of(">"), button -> {
            this.updateButtons(true);
        })
                .position(86, 10)
                .size(16, 16).tooltip(Tooltip.of(Text.of("Page " + (page + 1))))
                .build();
        this.addDrawableChild(this.nextButton);
        this.functionButton = ButtonWidget.builder(Text.of("Page " + page), button -> {})
                .position(26, 10)
                .size(60, 16)
                .build();
        this.addDrawableChild(this.functionButton);
        /*
        this.previousButton.visible = this.page > 1;
        this.functionButton.visible = true;
        this.nextButton.visible = this.page < this.inventory.size() / 27;

         */
    }

    @Unique
    private void updateButtons(boolean next) {
        if(next) {
            if(this.page > 1) this.swapInventory(this.page);
            this.page++;
            this.previousButton.visible = true;
            if(this.page >= this.inventory.size() / 27) this.nextButton.visible = false;
            this.swapInventory(this.page);
        } else {
            this.swapInventory(this.page);
            this.page--;
            if(this.page < this.inventory.size() / 27) {
                this.nextButton.visible = true;
                if(this.page <= 1) this.previousButton.visible = false;
                else this.swapInventory(this.page);
            }
        }
        this.previousButton.setTooltip(Tooltip.of(Text.of("Page " + (page - 1))));
        this.nextButton.setTooltip(Tooltip.of(Text.of("Page " + (page + 1))));
        this.functionButton.setMessage(Text.of("Page " + page));
    }

    @Inject(method = "removed", at = @At(value = "HEAD"))
    public void resetInventory(CallbackInfo ci) {
        this.swapInventory(this.page);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "render", at = @At(value = "HEAD"))
    public void updateButtonsVisibility(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        boolean visible = (((Object) this) instanceof CreativeInventoryScreen
                && CreativeInventoryScreen.selectedTab == Registries.ITEM_GROUP.getOrThrow(ItemGroups.INVENTORY))
                || ((Object) this) instanceof InventoryScreen;
        this.previousButton.visible = this.page > 1 && visible;
        this.functionButton.visible = visible;
        this.nextButton.visible = this.page < this.inventory.size() / 27 && visible;
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void tickButtons(CallbackInfo ci) {
        if(!this.nextButton.visible && 27 * page + 9 < this.inventory.main.size()) {
            this.nextButton.visible = true;
        }
    }

    @Unique
    public void swapInventory(int page) {
        if(page > 1) {
            int startIndex = 27 * (page - 1) + 9;
            ArrayList<ItemStack> stack;
            stack = new ArrayList<>(this.inventory.main.subList(startIndex, startIndex + 27));
            for(int i = 0; i < 27; i++) {
                this.inventory.setStack(startIndex + i + 5, this.inventory.getStack(i + 9));
                this.inventory.setStack(i + 9, stack.get(i));
            }
            ((IPlayerInventory) this.inventory).setContentChanged();
        }
    }
}

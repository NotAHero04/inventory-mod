package com.negativeonehero.inventorymod.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {

    @Unique
    private PlayerInventory inventory;

    @Unique
    private ButtonWidget previousButton;
    @Unique
    private ButtonWidget nextButton;
    @Unique
    private TextWidget pageText;
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
                .position(66, 10)
                .size(16, 16).tooltip(Tooltip.of(Text.of("Page " + (page + 1))))
                .build();
        this.addDrawableChild(this.nextButton);
        this.pageText = new TextWidget(26, 11, 40, 16, Text.of("Page " + page), this.textRenderer);
        this.addDrawableChild(this.pageText);
        this.previousButton.visible = false;
        this.nextButton.visible = true;
        this.pageText.visible = true;
    }

    @Unique
    private void updateButtons(boolean next) {
        if(next) {
            this.swapInventory(page);
            this.page++;
            if(this.page >= 2) {
                this.previousButton.visible = true;
                if(this.page >= this.inventory.size() / 27 + 1) this.nextButton.visible = false;
            }
        } else {
            this.swapInventory(page);
            this.page--;
            if(this.page < this.inventory.size() / 27 + 1) {
                this.nextButton.visible = true;
                if(this.page <= 1) this.previousButton.visible = false;
            }
        }
        this.swapInventory(page);
        this.previousButton.setTooltip(Tooltip.of(Text.of("Page " + (page - 1))));
        this.nextButton.setTooltip(Tooltip.of(Text.of("Page " + (page + 1))));
        this.pageText.setMessage(Text.of("Page " + page));
    }

    @Unique
    public void swapInventory(int page) {
        try {
            // We can only go to page 1 from page 2
            if(page == 1) this.swapInventory(2);
            else {
                int startIndex = 27 * page + 9;
                ArrayList<ItemStack> stack;
                try {
                    stack = (ArrayList<ItemStack>) this.inventory.main.subList(startIndex, startIndex + 26);
                } catch (IndexOutOfBoundsException e) {
                    stack = (ArrayList<ItemStack>) this.inventory.main.subList(startIndex, this.inventory.main.size() - 1);
                    while(stack.size() < 27) stack.add(ItemStack.EMPTY);
                }
                for(int i = 0; i < 27; i++) {
                    this.inventory.main.set(startIndex + i, this.inventory.main.get(i + 9));
                    this.inventory.main.set(i + 9, stack.get(i));
                }
            }
        } catch(IndexOutOfBoundsException ignored) {}
    }
}

package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.utils.SortingType;
import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen {
    @Shadow public abstract void tick();

    @Unique
    private PlayerInventory inventory;
    @Unique
    private IPlayerInventory iPlayerInventory;
    @Unique
    private ButtonWidget previousButton;
    @Unique
    private ButtonWidget nextButton;
    @Unique
    private ButtonWidget functionButton;
    @Unique
    private ButtonWidget sortingTypeButton;
    @Unique
    private boolean sorting = false;
    @Unique
    private int page = 1;
    @Unique
    private SortingType sortingType = SortingType.COUNT;
    @Unique
    private int ticksSinceAction = 0;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(T handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.inventory = inventory;
        this.iPlayerInventory = (IPlayerInventory) inventory;
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void init(CallbackInfo ci) {
        this.previousButton = ButtonWidget.builder(Text.of("<"), button -> this.update(false))
                .position(10, 10)
                .size(16, 16)
                .tooltip(Tooltip.of(Text.of("Page " + (page - 1))))
                .build();
        this.addDrawableChild(this.previousButton);
        this.nextButton = ButtonWidget.builder(Text.of(">"), button -> this.update(true))
                .position(86, 10)
                .size(16, 16)
                .tooltip(Tooltip.of(Text.of("Page " + (page + 1))))
                .build();
        this.addDrawableChild(this.nextButton);
        this.functionButton = ButtonWidget.builder(Text.of(""), button -> {
            this.sorting = !this.sorting;
            this.updateTooltip();
                })
                .position(26, 10)
                .size(60, 16)
                .build();
        this.addDrawableChild(this.functionButton);
        this.sortingTypeButton = ButtonWidget.builder(this.sortingType.message, button -> {
            this.sortingType = this.sortingType.next();
            button.setMessage(this.sortingType.message);
                })
                .position(10, 26)
                .size(92, 16)
                .build();
        this.addDrawableChild(this.sortingTypeButton);
        this.updateTooltip();
    }

    @Unique
    private void update(boolean next) {
        if (sorting) {
            this.iPlayerInventory.sort(next, this.page, this.sortingType);
            this.previousButton.active = false;
            this.nextButton.active = false;
        } else {
            if(next) {
                if (this.page > 1) this.iPlayerInventory.swapInventory(this.page);
                this.page++;
                this.previousButton.visible = true;
                if (this.page >= this.inventory.size() / 27) this.nextButton.visible = false;
                this.iPlayerInventory.swapInventory(this.page);
            } else {
                this.iPlayerInventory.swapInventory(this.page);
                this.page--;
                if (this.page < this.inventory.size() / 27) {
                    this.nextButton.visible = true;
                    if (this.page <= 1) this.previousButton.visible = false;
                    else this.iPlayerInventory.swapInventory(this.page);
                }
            }
            this.updateTooltip();
        }
    }

    @Unique
    private void updateTooltip() {
        if(this.sorting) {
            this.previousButton.setTooltip(Tooltip.of(Text.of("Sort Descending")));
            this.nextButton.setTooltip(Tooltip.of(Text.of("Sort Ascending")));
            this.functionButton.setMessage(Text.of("Sorting"));

        } else {
            this.previousButton.setTooltip(Tooltip.of(Text.of("Page " + (page - 1))));
            this.nextButton.setTooltip(Tooltip.of(Text.of("Page " + (page + 1))));
            this.functionButton.setMessage(Text.of("Inventory"));
        }
    }

    @SuppressWarnings("ConstantValue")
    @Unique
    private void updateButtons() {
        boolean visible = !(((Object) this) instanceof CreativeInventoryScreen)
                || CreativeInventoryScreen.selectedTab == Registries.ITEM_GROUP.getOrThrow(ItemGroups.INVENTORY);
        if(sorting) {
            this.previousButton.visible = visible;
            this.functionButton.visible = visible;
            this.nextButton.visible = visible;
            this.sortingTypeButton.visible = visible;
        } else {
            this.previousButton.visible = this.page > 1 && visible;
            this.functionButton.visible = visible;
            this.nextButton.visible = this.page < this.inventory.size() / 27 && visible;
            this.sortingTypeButton.visible = false;
        }
    }

    @Inject(method = "removed", at = @At(value = "HEAD"))
    public void resetInventory(CallbackInfo ci) {
        this.iPlayerInventory.swapInventory(this.page);
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void updateButtonsVisibility(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.updateButtons();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void tick(CallbackInfo ci) {
        if(this.ticksSinceAction >= 20) {
            if(!this.previousButton.active || !this.sorting) {
                this.previousButton.active = true;
                this.nextButton.active = true;
            }
            this.ticksSinceAction = 0;
        } else {
            this.ticksSinceAction++;
        }
        if(!this.sorting && !this.nextButton.visible && 27 * page + 14 < this.inventory.size()) {
            this.nextButton.visible = true;
            this.ticksSinceAction = 20;
        }
    }
    @Inject(method = "isClickOutsideBounds(DDIII)Z", at = @At(value = "RETURN"), cancellable = true)
    protected void isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> ci) {
        boolean oldClickOutsideBounds = ci.getReturnValue();
        boolean clickOutsideButtons = mouseX < 10 || mouseX > 102 || mouseY < 10 || mouseY > 26;
        if (!oldClickOutsideBounds || !clickOutsideButtons) ci.setReturnValue(false);
    }
}

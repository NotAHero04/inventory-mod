package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.SortingType;
import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin<T extends Container> extends Screen {
    @Unique
    private PlayerInventory inventory;
    @Unique
    private IPlayerInventory iPlayerInventory;
    @Unique
    private AbstractPressableButtonWidget previousButton;
    @Unique
    private AbstractPressableButtonWidget nextButton;
    @Unique
    private AbstractPressableButtonWidget functionButton;
    @Unique
    private AbstractPressableButtonWidget sortingTypeButton;
    @Unique
    private boolean sorting = false;
    @Unique
    private int page = 1;
    @Unique
    private SortingType sortingType = SortingType.COUNT;
    @Unique
    private int ticksSinceSorting = 0;

    @Unique
    private String previousTooltip = "";
    @Unique
    private String nextTooltip = "";
    @Unique
    private final ContainerScreen screen = (ContainerScreen) (Object) this;

    protected ContainerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(T handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.inventory = inventory;
        this.iPlayerInventory = (IPlayerInventory) inventory;
    }

    @Inject(method = "init", at = @At(value = "TAIL"))
    public void init(CallbackInfo ci) {
        this.previousButton = new AbstractPressableButtonWidget(10, 10, 16, 16, "<") {
            @Override
            public void onPress() {
                update(false);
            }

            @Override
            public void renderToolTip(int mouseX, int mouseY) {
                if (mouseX >= this.x && mouseX <= this.x+this.width
                        && mouseY >= this.y && mouseY <= this.y+this.height)
                    screen.renderTooltip(previousTooltip, mouseX, mouseY);
            }
        };
        this.nextButton = new AbstractPressableButtonWidget(86, 10, 16, 16, ">") {
            @Override
            public void onPress() {
                update(true);
            }

            @Override
            public void renderToolTip(int mouseX, int mouseY) {
                if (mouseX >= this.x && mouseX <= this.x+this.width
                        && mouseY >= this.y && mouseY <= this.y+this.height)
                    screen.renderTooltip(nextTooltip, mouseX, mouseY);
            }
        };
        this.functionButton = new ButtonWidget(26, 10, 60, 16, "Page " + page,
                button -> {
                    this.sorting = !this.sorting;
                    this.updateTooltip();
                });
        this.sortingTypeButton = new ButtonWidget(10, 26, 92, 16, this.sortingType.message,
                button -> {
                    this.sortingType = this.sortingType.next();
                    button.setMessage(this.sortingType.message);
                });
        this.addButton(this.previousButton);
        this.addButton(this.nextButton);
        this.addButton(this.functionButton);
        this.addButton(this.sortingTypeButton);
        this.updateTooltip();
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.previousButton.visible) this.previousButton.renderToolTip(mouseX, mouseY);
        if (this.nextButton.visible) this.nextButton.renderToolTip(mouseX, mouseY);
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
                if (this.page >= this.inventory.getInvSize() / 27) this.nextButton.visible = false;
                this.iPlayerInventory.swapInventory(this.page);
            } else {
                this.iPlayerInventory.swapInventory(this.page);
                this.page--;
                if (this.page < this.inventory.getInvSize() / 27) {
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
        if(sorting) {
            this.previousTooltip = "Sort Descending";
            this.nextTooltip = "Sort Ascending";
        } else {
            this.previousTooltip = "Page " + (page - 1);
            this.nextTooltip = "Page " + (page + 1);
        }
    }

    @SuppressWarnings("ConstantValue")
    @Unique
    private void updateButtons() {
        boolean visible = (((Object) this) instanceof CreativeInventoryScreen
                && CreativeInventoryScreen.selectedTab == ItemGroup.INVENTORY.getIndex())
                || ((Object) this) instanceof InventoryScreen;
        if(sorting) {
            this.previousButton.visible = visible;
            this.functionButton.visible = visible;
            this.nextButton.visible = visible;
            this.sortingTypeButton.visible = visible;
        } else {
            this.previousButton.visible = this.page > 1 && visible;
            this.functionButton.visible = visible;
            this.nextButton.visible = this.page < this.inventory.getInvSize() / 27 && visible;
            this.sortingTypeButton.visible = false;
        }
    }

    @Inject(method = "removed", at = @At(value = "HEAD"))
    public void resetInventory(CallbackInfo ci) {
        this.iPlayerInventory.swapInventory(this.page);
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void updateButtonsVisibility(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.updateButtons();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void tick(CallbackInfo ci) {
        if(!this.sorting && !this.nextButton.visible && 27 * page + 14 < this.inventory.getInvSize()) {
            this.nextButton.visible = true;
        }
        if(!this.previousButton.active) {
            if(this.ticksSinceSorting >= 20 || !this.sorting) {
                this.ticksSinceSorting = 0;
                this.previousButton.active = true;
                this.nextButton.active = true;
            } else {
                this.ticksSinceSorting++;
            }
        }
        if(sorting) {
            this.functionButton.setMessage("Sorting");
        } else {
            this.functionButton.setMessage("Page " + page + "/" + (this.inventory.getInvSize() - 14) / 27);
        }
    }
}

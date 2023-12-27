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
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends Screen {
    @Unique
    private PlayerInventory inventory;
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
    private String previousTooltip = "";
    @Unique
    private String nextTooltip = "";
    @Unique
    private final ContainerScreen screen = (ContainerScreen) (Object) this;

    protected ContainerScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(Container container, PlayerInventory playerInventory, Text name, CallbackInfo ci) {
        this.inventory = playerInventory;
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
        this.addButton(this.functionButton);
        this.addButton(this.nextButton);
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
            this.sort(next);
        } else {
            if(next) {
                if (this.page > 1) this.swapInventory(this.page);
                this.page++;
                this.previousButton.visible = true;
                if (this.page >= this.inventory.getInvSize() / 27) this.nextButton.visible = false;
                this.swapInventory(this.page);
            } else {
                this.swapInventory(this.page);
                this.page--;
                if (this.page < this.inventory.getInvSize() / 27) {
                    this.nextButton.visible = true;
                    if (this.page <= 1) this.previousButton.visible = false;
                    else this.swapInventory(this.page);
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
            this.functionButton.setMessage("Sorting");
        } else {
            this.previousTooltip = "Page " + (page - 1);
            this.nextTooltip = "Page " + (page + 1);
            this.functionButton.setMessage("Page " + page);
        }
    }

    @Unique
    public void sort(boolean ascending) {
        this.swapInventory(this.page);
        ArrayList<ItemStack> stacks = new ArrayList<>();
        int emptySlots = 0;
        for(int i = 9; i < this.inventory.main.size(); i++) {
            ItemStack stack = this.inventory.main.get(i);
            if (stack.isEmpty()) emptySlots++;
            else stacks.add(stack);
        }
        this.sortingType.sort(stacks, ascending);
        for(int i = 0; i < emptySlots; i++) {
            stacks.add(ItemStack.EMPTY);
        }
        for(int i = 0; i < stacks.size(); i++) {
            this.inventory.setInvStack(i + (i > 26 ? 14 : 9), stacks.get(i));
        }
        ((IPlayerInventory) this.inventory).setContentChanged();
        this.swapInventory(this.page);
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
        this.swapInventory(this.page);
    }

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void updateButtonsVisibility(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.updateButtons();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void tickButtons(CallbackInfo ci) {
        if(!this.sorting && !this.nextButton.visible && 27 * page + 9 < this.inventory.main.size()) {
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
                this.inventory.setInvStack(startIndex + i + 5, this.inventory.getInvStack(i + 9));
                this.inventory.setInvStack(i + 9, stack.get(i));
            }
            ((IPlayerInventory) this.inventory).setContentChanged();
        }
    }
}

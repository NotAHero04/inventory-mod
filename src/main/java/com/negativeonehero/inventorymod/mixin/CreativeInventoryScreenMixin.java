package com.negativeonehero.inventorymod.mixin;

import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin<T extends Container> extends ContainerScreen<T> {

    public CreativeInventoryScreenMixin(T handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean oldClickOutsideBounds = mouseX < (double)left || mouseY < (double)top
                || mouseX >= (double)(left + this.containerWidth) || mouseY >= (double)(top + this.containerHeight);
        boolean clickOutsideButtons = mouseX < 10 || mouseX > 102 || mouseY < 10 || mouseY > 26;
        return oldClickOutsideBounds && clickOutsideButtons;
    }
}

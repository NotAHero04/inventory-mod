package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.InventoryMod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Redirect(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"))
    private int draw(DrawContext instance, TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
        if (!text.isEmpty()) {
            try {
                String newText = "";
                try {
                    NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);
                    fmt.setRoundingMode(RoundingMode.FLOOR);
                    fmt.setMaximumFractionDigits(1);
                    newText = fmt.format(Integer.parseInt(text));
                } catch (NumberFormatException e) {
                    InventoryMod.LOGGER.warn("Failed to parse text!");
                }
                float scale = 0.55f;
                instance.getMatrices().scale(scale, scale, 1);
                return instance.drawText(renderer, newText, (int) ((x + renderer.getWidth(text)) / scale - renderer.getWidth(newText) - 3), (int) (y / scale + 4), color, shadow);
            } catch (Exception e) {
                e.printStackTrace();
                return instance.drawText(renderer, text, x, y, color, shadow);
            }
        } else return instance.drawText(renderer, text, x, y, color, shadow);
    }
}

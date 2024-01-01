package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.InventoryMod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Shadow public abstract int drawText(TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow);

    @Unique
    private String formatDouble(double number, int decimalPlaces) {
        DecimalFormat df = new DecimalFormat("#." + StringUtils.repeat('#', decimalPlaces));
        df.setRoundingMode(RoundingMode.FLOOR);
        Double n = number;
        return df.format(n);
    }

    @Redirect(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;IIIZ)I"))
    private int draw(DrawContext instance, TextRenderer renderer, String text, int x, int y, int color, boolean shadow) {
        if (!text.isEmpty()) {
            try {
                String newText = text;
                try {
                    int count = Integer.parseInt(Objects.requireNonNull(Formatting.strip(text)));
                    if (count >= 1e9)
                        newText = Formatting.AQUA + formatDouble(count / 1000000000d, 2) + "B";
                    else if (count >= 1e6)
                        newText = Formatting.GREEN + formatDouble(count / 1000000d, 2) + "M";
                    else if (count >= 1e3)
                        newText = Formatting.YELLOW + formatDouble(count / 1000d, 1) + "K";
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

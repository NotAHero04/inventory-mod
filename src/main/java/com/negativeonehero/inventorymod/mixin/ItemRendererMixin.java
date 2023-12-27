package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.InventoryMod;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Unique
    private String formatDouble(double number, int decimalPlaces) {
        DecimalFormat df = new DecimalFormat("#." + StringUtils.repeat('#', decimalPlaces));
        df.setRoundingMode(RoundingMode.FLOOR);
        Double n = number;
        return df.format(n);
    }

    @Redirect(method = "renderGuiItemOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"))
    private int draw(TextRenderer renderer, String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light) {
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
            }
            catch (NumberFormatException e) {
                InventoryMod.LOGGER.warn("Failed to parse text!");
            }

            float scale = 0.55f;
            matrix.scale(scale, scale, 1);
            return renderer.draw(newText, (x+renderer.getWidth(text))/scale-renderer.getWidth(newText)-3, y/scale+4, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
        }
        catch (Exception e) {
            e.printStackTrace();
            return renderer.draw(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
        }
    }
}

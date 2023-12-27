package com.negativeonehero.inventorymod.mixin;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    private int count;
    @Shadow
    public abstract int getCount();

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V")
    void onDeserialization(CompoundTag tag, CallbackInfo callbackInformation) {
        if (tag.contains("CountInteger")) {
            this.count = tag.getInt("CountInteger");
        }
    }

    @Inject(at = @At ("TAIL"), method = "toTag")
    void onSerialization(CompoundTag tag, CallbackInfoReturnable<CompoundTag> callbackInformationReturnable) {
        if (this.count > Byte.MAX_VALUE) {
            tag.putInt("CountInteger", this.count);
            tag.putByte("Count", Byte.MAX_VALUE);
        }
    }

    @Inject(method = "getTooltip", at = @At ("RETURN"))
    private void addOverflowTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (this.getCount() > 1000) {
            List<Text> texts = cir.getReturnValue();
            MutableText text = (MutableText) texts.get(0);
            texts.set(0, text.append(((MutableText) Text.method_30163(" x")).formatted(Formatting.GRAY))
                    .append(((MutableText)Text.method_30163(NumberFormat.getNumberInstance(Locale.US).format(this.getCount()))).formatted(Formatting.GRAY)));
        }
    }
}

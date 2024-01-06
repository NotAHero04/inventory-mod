package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.impl.IScreenHandler;
import com.negativeonehero.inventorymod.network.Packets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin implements IScreenHandler {

    @Final
    @Shadow
    public DefaultedList<Slot> slots;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }

        Slot slot;
        ItemStack itemStack;
        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (!itemStack.isEmpty() && ItemStack.canCombine(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxCount() && j >= 0) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot.markDirty();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxCount()) {
                        stack.decrement(stack.getMaxCount() - itemStack.getCount());
                        itemStack.setCount(stack.getMaxCount());
                        slot.markDirty();
                        bl = true;
                    }
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (fromLast) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot = this.slots.get(i);
                itemStack = slot.getStack();
                if (itemStack.isEmpty() && slot.canInsert(stack)) {
                    if (stack.getCount() > slot.getMaxItemCount()) {
                        slot.setStack(stack.split(slot.getMaxItemCount()));
                    } else {
                        slot.setStack(stack.split(stack.getCount()));
                    }

                    slot.markDirty();
                    bl = true;
                    break;
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return bl;
    }

    @Redirect(method = "calculateStackSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;floor(F)I"))
    private static int floor(float input, Set<Slot> slots, int mode, ItemStack stack) {
        return stack.getCount() / slots.size();
    }


    @Unique
    public void swapTrackedSlots(int page, boolean client) {
        if(client) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(page);
            ClientPlayNetworking.send(Packets.SWAP_PACKET_ID, buf);
        }
        // Only PlayerScreenHandler has a slot after inventory slots for offhand
        int slotAdjustment = ((ScreenHandler) (Object) this) instanceof PlayerScreenHandler ? 1 : 0;
        int newSlotIndex = (page - 1) * 27 + 9;
        for (int i = 26, j = ((ScreenHandler) (Object) this).slots.size() - (10 + slotAdjustment); i >= 0; i--, j--) {
            ((ScreenHandler) (Object) this).slots.get(j).index = newSlotIndex + i + (page > 1 ? 5 : 0);
        }
    }
}
package com.negativeonehero.inventorymod.mixin;

import com.negativeonehero.inventorymod.impl.IScreenHandler;
import com.negativeonehero.inventorymod.utils.PlayerUtility;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.apache.commons.lang3.Range;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

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
                        slot.setStackNoCallbacks(stack.split(slot.getMaxItemCount()));
                    } else {
                        slot.setStackNoCallbacks(stack.split(stack.getCount()));
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
        // Only PlayerScreenHandler has a slot after inventory slots for offhand
        List<DefaultedList<Slot>> sList = PlayerUtility.players().stream().map(i -> i.currentScreenHandler.slots).toList();
        DefaultedList<Slot> cSlots = sList.get(0);
        DefaultedList<Slot> sSlots = sList.get(1);
        int slotStart = IntStream.range(0, sSlots.size()).filter(i -> sSlots.get(i).inventory instanceof PlayerInventory
                && !Range.between(36, 39).contains(sSlots.get(i).getIndex())).findFirst().orElse(-1);
        int newSlotIndex = (page - 1) * 27 + (page > 1 ? 14 : 9);
        IntStream.range(0, 27).parallel().forEachOrdered(i -> {
            if(((ScreenHandler) (Object) this) instanceof PlayerScreenHandler) cSlots.get(slotStart + i).index = newSlotIndex + i;
            sSlots.get(slotStart + i).index = newSlotIndex + i;
        });
    }
}

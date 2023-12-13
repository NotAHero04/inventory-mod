package com.negativeonehero.inventorymod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.function.Predicate;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements Inventory {
    @Shadow
    public DefaultedList<ItemStack> main;
    @Shadow
    public DefaultedList<ItemStack> armor;
    @Shadow
    public DefaultedList<ItemStack> offHand;
    @Unique
    public ArrayList<ItemStack> mainExtras;
    @Shadow
    public PlayerEntity player;
    @Shadow
    public int selectedSlot;
    @Shadow
    abstract boolean canStackAddMore(ItemStack existingStack, ItemStack stack);
    @Shadow
    abstract int addStack(int slot, ItemStack stack);


    @Inject(method = "<init>", at = @At("TAIL"))
    public void constructor(CallbackInfo ci) {
        this.mainExtras = new ArrayList<>(0);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getEmptySlot() {
        for(int i = 0; i < this.main.size(); ++i) {
            if (this.main.get(i).isEmpty()) {
                return i;
            }
        }
        for(int i = 0; i < this.mainExtras.size(); ++i) {
            if (this.mainExtras.get(i).isEmpty()) {
                return i + 41;
            }
        }

        return -1;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public NbtList writeNbt(NbtList nbtList) {
        int i;
        NbtCompound nbtCompound;
        for(i = 0; i < this.main.size(); ++i) {
            if (!this.main.get(i).isEmpty()) {
                nbtCompound = new NbtCompound();
                nbtCompound.putInt("Slot", i);
                this.main.get(i).writeNbt(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        for(i = 0; i < this.armor.size(); ++i) {
            if (!this.armor.get(i).isEmpty()) {
                nbtCompound = new NbtCompound();
                nbtCompound.putInt("Slot", i + 36);
                this.armor.get(i).writeNbt(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        for(i = 0; i < this.offHand.size(); ++i) {
            if (!this.offHand.get(i).isEmpty()) {
                nbtCompound = new NbtCompound();
                nbtCompound.putInt("Slot", i + 40);
                this.offHand.get(i).writeNbt(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        for(i = 0; i < this.mainExtras.size(); ++i) {
            if(!this.mainExtras.get(i).isEmpty()) {
                nbtCompound = new NbtCompound();
                nbtCompound.putInt("Slot", i + 41);
                this.mainExtras.get(i).writeNbt(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }

        return nbtList;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void readNbt(NbtList nbtList) {
        this.main.clear();
        this.armor.clear();
        this.offHand.clear();

        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getInt("Slot");
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
            if (!itemStack.isEmpty()) {
                if (j >= 0 && j < this.main.size()) {
                    this.main.set(j, itemStack);
                } else if (j >= 36 && j < this.armor.size() + 36) {
                    this.armor.set(j - 36, itemStack);
                } else if (j >= 40 && j < this.offHand.size() + 40) {
                    this.offHand.set(j - 40, itemStack);
                } else {
                    this.mainExtras.add(itemStack);
                }
            }
        }
    }

    /**
     * @author
     * @reason
     */

    @Overwrite
    public int getOccupiedSlotWithRoomForStack(ItemStack stack) {
        if (this.canStackAddMore(this.getStack(this.selectedSlot), stack)) {
            return this.selectedSlot;
        } else if (this.canStackAddMore(this.getStack(40), stack)) {
            return 40;
        } else {
            for(int i = 0; i < this.main.size(); ++i) {
                if (this.canStackAddMore(this.main.get(i), stack)) {
                    return i;
                }
            }
            for(int i = 0; i < this.mainExtras.size(); ++i) {
                if (this.canStackAddMore(this.mainExtras.get(i), stack)) {
                    return i + 41;
                }
            }

            return -1;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int remove(Predicate<ItemStack> shouldRemove, int maxCount, Inventory craftingInventory) {
        int i = 0;
        boolean bl = maxCount == 0;
        i += Inventories.remove(this, shouldRemove, maxCount - i, bl);
        i += Inventories.remove(craftingInventory, shouldRemove, maxCount - i, bl);
        for(ItemStack stack : this.mainExtras)
            i += Inventories.remove(stack, shouldRemove, maxCount - i, bl);
        ItemStack itemStack = this.player.currentScreenHandler.getCursorStack();
        i += Inventories.remove(itemStack, shouldRemove, maxCount - i, bl);
        if (itemStack.isEmpty()) {
            this.player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
        }

        return i;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private int addStack(ItemStack stack) {
        int i = this.getOccupiedSlotWithRoomForStack(stack);
        if (i == -1) {
            i = this.getEmptySlot();
        }

        return i == -1 ? stack.getCount() : this.addStack(i, stack);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean insertStack(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else {
            try {
                if (stack.isDamaged()) {
                    if (slot == -1) {
                    }
                    System.out.println(slot);
                    if (slot >= 0 && slot < 36) {
                        this.main.set(slot, stack.copyAndEmpty());
                        this.main.get(slot).setBobbingAnimationTime(5);
                        return true;
                    } else if (slot >= 41) {
                        this.mainExtras.set(slot, stack.copyAndEmpty());
                        this.mainExtras.get(slot).setBobbingAnimationTime(5);
                        return true;
                    }  else if (this.player.getAbilities().creativeMode) {
                        stack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;
                    do {
                        i = stack.getCount();
                        if (slot == -1) {
                            stack.setCount(this.addStack(stack));
                        } else {
                            stack.setCount(this.addStack(slot, stack));
                        }
                    } while(!stack.isEmpty() && stack.getCount() < i);

                    if (stack.getCount() == i && this.player.getAbilities().creativeMode) {
                        stack.setCount(0);
                        return true;
                    } else {
                        return stack.getCount() < i;
                    }
                }
            } catch (Throwable var6) {
                CrashReport crashReport = CrashReport.create(var6, "Adding item to inventory");
                CrashReportSection crashReportSection = crashReport.addElement("Item being added");
                crashReportSection.add("Item ID", Item.getRawId(stack.getItem()));
                crashReportSection.add("Item data", stack.getDamage());
                crashReportSection.add("Item name", () -> {
                    return stack.getName().getString();
                });
                throw new CrashException(crashReport);
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int size() {
        return this.main.size() + this.armor.size() + this.offHand.size() + this.mainExtras.size();
    }
}

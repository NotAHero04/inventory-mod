package com.negativeonehero.inventorymod.mixin;

import com.google.common.collect.ImmutableList;
import com.negativeonehero.inventorymod.ExtendableItemStackDefaultedList;
import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements Inventory, IPlayerInventory {
    @Final
    @Shadow
    @Mutable
    public DefaultedList<ItemStack> main;
    @Final
    @Shadow
    @Mutable
    public DefaultedList<ItemStack> armor;
    @Final
    @Shadow
    @Mutable
    public DefaultedList<ItemStack> offHand;
    @Final
    @Shadow
    public PlayerEntity player;
    @Shadow
    public int selectedSlot;
    @Shadow
    protected abstract boolean canStackAddMore(ItemStack existingStack, ItemStack stack);
    @Shadow
    protected abstract int addStack(int slot, ItemStack stack);
    @Unique
    private boolean needsToSync = true;
    @Shadow @Final @Mutable
    private List<DefaultedList<ItemStack>> combinedInventory;
    @Unique
    private int contentChanged = 0;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(PlayerEntity player, CallbackInfo ci) {
        this.main = ExtendableItemStackDefaultedList.ofSize(36, ItemStack.EMPTY);
        this.combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);
    }

    // The inventory is rearranged for extra slots in the `main` part
    // Slot 0-35 and 41+ is for main
    // Slot 36-39 is for armor
    // Slot 40 is for off-hand

    @Unique
    public int invSlotFromMain(int mainSlot) {
        return mainSlot + (mainSlot > 35 ? 5 : 0);
    }

    @Unique
    public int mainSlotFromInv(int invSlot) {
        return invSlot - (invSlot > 35 ? 5 : 0);
    }

    @Unique
    public int getLastEmptySlot() {
        int ret = 0;
        int a = 0;
        while((a = this.getEmptySlotFromIndex(a + 1)) != -1) ret = a;
        return ret;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getEmptySlot() {
        int emptySlot = this.getEmptySlotFromIndex(0);
        if(emptySlot == -1) {
            for (int k = 0; k < 27; k++) {
                this.main.add(ItemStack.EMPTY);
            }
            return this.getEmptySlot();
        }
        return emptySlot;
    }

    @Unique
    public int getEmptySlotFromIndex(int index) {
        for(int i = index; i < this.main.size(); ++i) {
            if (this.main.get(i).isEmpty()) {
                return invSlotFromMain(i);
            }
        }
        return -1;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setStack(int slot, ItemStack stack) {
        // The OG method avoids using conditionals
        if(slot == 40) this.offHand.set(0, stack);
        else if(slot >= 36 && slot < 40) this.armor.set(slot - 36, stack);
        else this.main.set(mainSlotFromInv(slot), stack);
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
                nbtCompound.putInt("Slot", invSlotFromMain(i));
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
                nbtCompound.putInt("Slot", i + 40);;
                this.offHand.get(i).writeNbt(nbtCompound);
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
                if (j >= 40 && j < this.offHand.size() + 40) {
                    this.offHand.set(0, itemStack);
                } else if (j >= 36 && j < this.armor.size() + 36) {
                    this.armor.set(j - 36, itemStack);
                } else if (j >= 0 && j < 36) {
                    this.main.set(j, itemStack);
                } else {
                    this.main.add(itemStack);
                }
            }
        }
        while(this.main.size() % 27 != 9) this.main.add(ItemStack.EMPTY);
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
                    return invSlotFromMain(i);
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
    private int addStack(ItemStack stack) {
        // Fix duping while picking items on swapping inventories
        if(this.contentChanged > 0) {
            this.contentChanged--;
            return this.addStack(stack);
        }
        int i = this.getOccupiedSlotWithRoomForStack(stack);
        if (i == -1) {
            i = this.getEmptySlot();
        }

        return i == -1 ? stack.getCount() : this.addStack(i, stack);
    }

    // Technically removeStack() can also cause duping, but it's impossible to recreate

    /**
     * @author
     * @reason
     */
    @Overwrite
    public ItemStack getStack(int slot) {
        if(slot == 40) return this.offHand.get(0);
        if(slot >= 36 && slot < 40) return this.armor.get(slot - 36);
        return this.main.get(mainSlotFromInv(slot));
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
                        slot = this.getEmptySlot();
                    }
                    if (slot <= 35 || slot >= 41) {
                        this.main.set(mainSlotFromInv(slot), stack.copyAndEmpty());
                        this.main.get(mainSlotFromInv(slot)).setBobbingAnimationTime(5);
                        return true;
                    } else if (this.player.getAbilities().creativeMode) {
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
                crashReportSection.add("Item name", () -> stack.getName().getString());
                throw new CrashException(crashReport);
            }
        }
    }

    // A very hacky way to force the inventory to always sync
    @Inject(method = "updateItems", at = @At(value = "TAIL"))
    public void syncInventories(CallbackInfo ci) throws InterruptedException {
        if(this.needsToSync && this.player instanceof ServerPlayerEntity) {
            // Fixes race condition upon rejoining a world... by simply waiting
            while(MinecraftClient.getInstance().player == null) Thread.sleep(100);
            MinecraftClient.getInstance().player.inventory.main = this.main;
            MinecraftClient.getInstance().player.inventory.combinedInventory = this.combinedInventory;
            this.needsToSync = false;
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int size() {
        return this.main.size() + this.armor.size() + this.offHand.size();
    }

    @Unique
    public void setContentChanged() { this.contentChanged++; }
}

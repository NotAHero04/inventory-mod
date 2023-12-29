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
    @Unique
    public ExtendableItemStackDefaultedList mainExtras;
    @Final
    @Shadow
    public PlayerEntity player;
    @Shadow
    public int selectedSlot;
    @Shadow
    protected abstract boolean canStackAddMore(ItemStack existingStack, ItemStack stack);
    @Unique
    private boolean needsToSync = true;
    @Shadow @Final @Mutable
    private List<DefaultedList<ItemStack>> combinedInventory;

    @Shadow public abstract ItemStack removeStack(int slot);

    @Unique
    private int contentChanged = 0;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(PlayerEntity player, CallbackInfo ci) {
        this.mainExtras = ExtendableItemStackDefaultedList.ofSize(0, ItemStack.EMPTY);
        this.combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);
    }

    /* The inventory is rearranged for extra slots in the `main` part
     * Slot 0-35 - main
     * Slot 36-39 - armor
     * Slot 40 - off-hand
     * Slot 41+ - main extras
     */

    /**
     * @author
     * @reason
     */
    @Overwrite
    public int getEmptySlot() {
        int emptySlot = this.getEmptySlotFromIndex(0);
        if(emptySlot == -1) {
            for (int k = 0; k < 27; k++) {
                this.mainExtras.add(ItemStack.EMPTY);
            }
            return this.getEmptySlot();
        }
        return emptySlot;
    }

    @Unique
    public int getEmptySlotFromIndex(int index) {
        for(int i = index; i < 36; ++i) {
            if (this.main.get(i).isEmpty()) {
                return i;
            }
        }
        for(int i = index - 36; i < this.mainExtras.size(); i++) {
            if(this.mainExtras.get(i).isEmpty()) {
                return i + 36;
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
        else if(slot < 36) this.main.set(slot, stack);
        else this.mainExtras.set(slot - 41, stack);
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
            if (!this.mainExtras.get(i).isEmpty()) {
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
        this.mainExtras.clear();

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
                    this.mainExtras.add(itemStack);
                }
            }
        }
        while(this.mainExtras.size() % 27 != 0) this.mainExtras.add(ItemStack.EMPTY);
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

    /**
     * @author
     * @reason
     */
    @Overwrite
    private int addStack(int slot, ItemStack stack) {
        Item item = stack.getItem();
        int i = stack.getCount();
        ItemStack itemStack = this.getStack(slot);
        if (itemStack.isEmpty()) {
            itemStack = new ItemStack(item, 0);
            if (stack.hasNbt()) {
                assert stack.getNbt() != null;
                itemStack.setNbt(stack.getNbt().copy());
            }

            this.setStack(slot, itemStack);
        }

        int j = Math.min(i, itemStack.getMaxCount() - itemStack.getCount());
        j = Math.min(j, this.getMaxCountPerStack() - itemStack.getCount());

        if (j != 0) {
            i -= j;
            itemStack.increment(j);
            itemStack.setBobbingAnimationTime(5);
        }
        return i;
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
        if(slot < 36) return this.main.get(slot);
        return this.mainExtras.get(slot - 41);
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
                    if (slot <= 35) {
                        this.main.set(slot, stack.copyAndEmpty());
                        this.main.get(slot).setBobbingAnimationTime(5);
                        return true;
                    } else if (slot >= 41) {
                        this.mainExtras.set(slot - 41, stack.copyAndEmpty());
                        this.mainExtras.get(slot - 41).setBobbingAnimationTime(5);
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
            ((IPlayerInventory) MinecraftClient.getInstance().player.inventory).setMainExtras(this.mainExtras);
            for(int i = 0; i < 36; i++)
                MinecraftClient.getInstance().player.inventory.setStack(i, this.getStack(i));
            this.needsToSync = false;
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

    // Interface: IPlayerInventory

    @Unique
    public ItemStack removeExtrasStack(int slot) { return this.removeStack(slot + 41); }

    @Unique
    public void setContentChanged() { this.contentChanged++; }

    @Unique
    public void setMainExtras(ExtendableItemStackDefaultedList mainExtras1) { this.mainExtras = mainExtras1; }

    @Unique
    public ExtendableItemStackDefaultedList getMainExtras() {return this.mainExtras; }

    @Unique
    public int getLastEmptySlot() {
        int ret = 0;
        int a = 0;
        while((a = this.getEmptySlotFromIndex(a + 1)) != -1) ret = a;
        return ret;
    }
}

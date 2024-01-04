package com.negativeonehero.inventorymod.mixin;

import com.google.common.collect.ImmutableList;
import com.negativeonehero.inventorymod.utils.ExtendableItemStackDefaultedList;
import com.negativeonehero.inventorymod.utils.SortingType;
import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import com.negativeonehero.inventorymod.network.Packets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin implements Inventory, IPlayerInventory {
    @Shadow @Mutable public DefaultedList<ItemStack> main;
    @Final @Shadow @Mutable public DefaultedList<ItemStack> armor;
    @Final @Shadow @Mutable public DefaultedList<ItemStack> offHand;
    @Final @Shadow public PlayerEntity player;
    @Shadow public int selectedSlot;
    @Shadow protected abstract boolean canStackAddMore(ItemStack existingStack, ItemStack stack);
    @Shadow public abstract boolean insertStack(ItemStack stack);
    @Shadow protected abstract int addStack(ItemStack stack);
    @Shadow public abstract ItemStack removeStack(int slot);
    @Shadow protected abstract int addStack(int slot, ItemStack stack);
    @Shadow public abstract void markDirty();
    @Shadow @Mutable public List<DefaultedList<ItemStack>> combinedInventory;

    @Unique public ExtendableItemStackDefaultedList mainExtras;
    @Unique private boolean needsToSync = true;
    @Unique private int contentChanged = 0;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void constructor(PlayerEntity player, CallbackInfo ci) {
        this.mainExtras = ExtendableItemStackDefaultedList.ofSize(0, ItemStack.EMPTY);
        this.combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand, this.mainExtras);
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
        int i = 0;
        for(ItemStack stack : main) {
            if(stack.isEmpty()) return i;
            i++;
        }
        i += 5; // We don't check slot 36-40
        for(ItemStack stack : mainExtras) {
            if(stack.isEmpty()) return i;
            i++;
        }
        for (int k = 0; k < 27; k++) {
            this.mainExtras.add(ItemStack.EMPTY);
            this.needsToSync = true;
        }
        return this.getEmptySlot();
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

    @Inject(method = "addStack(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    public void addStackFix(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if(this.contentChanged > 0) {
            this.contentChanged--;
            cir.setReturnValue(this.addStack(stack));
        }
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
    public void syncInventories(CallbackInfo ci) {
        if(this.needsToSync && this.player instanceof ServerPlayerEntity) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(this.mainExtras.size());
            for(ItemStack stack : this.mainExtras) buf.writeItemStack(stack);
            ServerPlayNetworking.send((ServerPlayerEntity) this.player, Packets.UPDATE_EXTRA_SLOTS_PACKET_ID, buf);
            this.markDirty();
            this.needsToSync = false;
        }
    }

    @Inject(method = "size", at = @At("TAIL"), cancellable = true)
    public void size(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.main.size() + this.armor.size() + this.offHand.size() + this.mainExtras.size());
    }

    // Interface: IPlayerInventory

    @Unique
    public ItemStack removeExtrasStack(int slot) { return this.removeStack(slot + 41); }

    @Unique
    public void setContentChanged() {
        this.contentChanged++;
    }

    @Unique
    public void needsToSync() { this.needsToSync = true; }

    @Unique
    public ExtendableItemStackDefaultedList getMainExtras() { return this.mainExtras; }

    @Unique
    public List<ItemStack> getCombinedMainNoHotbar() {
        return Stream.concat(this.main.subList(9, 36).stream(), this.mainExtras.stream()).toList();
    }

    @Unique
    public void swapInventory(int page) {
        if(this.player instanceof ClientPlayerEntity) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(page);
            ClientPlayNetworking.send(Packets.SWAP_PACKET_ID, buf);
        }
        if(page > 1) {
            int startIndex = 27 * (page - 2);
            ArrayList<ItemStack> stack;
            stack = new ArrayList<>(this.getMainExtras()
                    .subList(startIndex, startIndex + 27));
            for(int i = 0; i < 27; i++) {
                this.setStack(startIndex + i + 41, this.getStack(i + 9));
                this.setStack(i + 9, stack.get(i));
            }
            this.setContentChanged();
        }
    }

    @Unique
    public void sort(boolean ascending, int page, SortingType sortingType) {
        if(this.player instanceof ClientPlayerEntity) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBoolean(ascending);
            buf.writeInt(page);
            buf.writeInt(sortingType.ordinal());
            ClientPlayNetworking.send(Packets.SORT_PACKET_ID, buf);
        }
        this.swapInventory(page);
        ArrayList<ItemStack> stacks = new ArrayList<>();
        int emptySlots = 0;
        for(int i = this.main.size() - 1; i >= 9; i--) {
            this.insertStack(this.removeStack(i));
        }
        this.setContentChanged();
        for(int i = this.getMainExtras().size() - 1; i >= 0; i--) {
            this.insertStack(this.removeExtrasStack(i));
        }
        this.setContentChanged();
        for(ItemStack stack : this.getCombinedMainNoHotbar()) {
            if (stack.isEmpty()) emptySlots++;
            else stacks.add(stack);
        }
        sortingType.sort(stacks, ascending);
        for(int i = 0; i < emptySlots; i++) {
            stacks.add(ItemStack.EMPTY);
        }
        for(int i = 0; i < stacks.size(); i++) {
            this.setStack(i + (i > 26 ? 14 : 9), stacks.get(i));
        }
        this.setContentChanged();
        this.swapInventory(page);
    }
}

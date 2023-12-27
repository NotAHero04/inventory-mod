package com.negativeonehero.inventorymod.network;

import com.negativeonehero.inventorymod.SortingType;
import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class Packets {
    public static final Identifier UPDATE_EXTRA_SLOTS_PACKET_ID =
            new Identifier("inventorymod", "update_extra_slots");
    public static final Identifier SORT_PACKET_ID = new Identifier("inventorymod", "sort");
    public static final Identifier SWAP_PACKET_ID = new Identifier("inventorymod", "swap");

    @Environment(EnvType.CLIENT)
    public static void initClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_EXTRA_SLOTS_PACKET_ID, (client, handler, buf, responseSender) -> {
            int extraSlots = buf.readInt();
            ArrayList<ItemStack> stacks = new ArrayList<>();
            for(int i = 0; i < extraSlots; i++) {
                stacks.add(i, buf.readItemStack());
            }
            client.execute(() -> {
                assert client.player != null;
                PlayerInventory playerInventory = client.player.inventory;
                IPlayerInventory inventory = (IPlayerInventory) playerInventory;
                while(inventory.getMainExtras().size() < extraSlots) {
                    inventory.getMainExtras().add(ItemStack.EMPTY);
                }
                for(int i = 0; i < extraSlots; i++) {
                    inventory.getMainExtras().set(i, stacks.get(i));
                }
            });
        });
    }

    public static void initServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SORT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            boolean ascending = buf.readBoolean();
            int page = buf.readInt();
            SortingType sortingType = SortingType.types[buf.readInt()];
            server.execute(() -> {
                ((IPlayerInventory)player.inventory).sort(ascending, page, sortingType);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SWAP_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            int x = buf.readInt();
            server.execute(() -> {
                ((IPlayerInventory)player.inventory).swapInventory(x);
            });
        });
    }
}

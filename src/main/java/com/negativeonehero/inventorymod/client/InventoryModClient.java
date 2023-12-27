package com.negativeonehero.inventorymod.client;

import com.negativeonehero.inventorymod.network.Packets;
import net.fabricmc.api.ClientModInitializer;

public class InventoryModClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        Packets.initClientPackets();
    }
}

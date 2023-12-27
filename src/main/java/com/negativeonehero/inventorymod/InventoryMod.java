package com.negativeonehero.inventorymod;

import com.negativeonehero.inventorymod.network.Packets;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InventoryMod implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("inventorymod");
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Mom, look! Inventory Mod 0.5.5-beta.2 is loading!");
		Packets.initServerPackets();
	}
}

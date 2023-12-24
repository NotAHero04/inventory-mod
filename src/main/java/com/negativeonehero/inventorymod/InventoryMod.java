package com.negativeonehero.inventorymod;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.fabricmc.api.ModInitializer;


public class InventoryMod implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("Inventory Mod");
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Mom, look! Inventory Mod 0.9.0-beta.1 is loading!");
	}
}

package com.negativeonehero.inventorymod;

import com.negativeonehero.inventorymod.utils.InventoryModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InventoryMod implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("inventorymod");
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Mom, look! Inventory Mod 0.9.0-beta.3 is loading!");
		InventoryModConfig.init();
	}
}

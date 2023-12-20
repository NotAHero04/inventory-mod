package com.negativeonehero.inventorymod;

import net.fabricmc.api.ModInitializer;


public class InventoryMod implements ModInitializer {
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.getLogger("Inventory Mod").log(System.Logger.Level.INFO,"Inventory Mod 0.9.0-alpha.3 loading...");
	}
}

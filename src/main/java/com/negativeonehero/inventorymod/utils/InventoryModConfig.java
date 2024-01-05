package com.negativeonehero.inventorymod.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.negativeonehero.inventorymod.InventoryMod;
import net.minecraft.util.JsonHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class InventoryModConfig {
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .serializeNulls()
            .create();
    private static final String FILE = "./config/inventorymod.json";
    private static File file;
    public static boolean bigInteger;

    public static void init() {
        try {
            file = new File(FILE);
            boolean fileCreated = file.createNewFile();
            writeConfig(fileCreated);
            readConfig();
        } catch (Exception e) {
            InventoryMod.LOGGER.error("Initializing config failed, Inventory Mod is probably about to crash!");
        }
    }

    private static void readConfig() {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonObject parser = JsonHelper.deserialize(reader);
            JsonElement element = parser.get("max_stack_size");
            if (element == null) bigInteger = false;
            else bigInteger = element.getAsBoolean();
        } catch (IOException e) {
            InventoryMod.LOGGER.error("Reading config failed, Inventory Mod is probably about to crash!");
        }
    }

    public static void writeConfig(boolean writeDefaults) {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            JsonObject obj = new JsonObject();
            obj.addProperty("max_stack_size", !writeDefaults && bigInteger);
            writer.write(GSON.toJson(obj));
        } catch (IOException e) {
            InventoryMod.LOGGER.error("Writing config failed, no changes were saved!");
        }
    }
}

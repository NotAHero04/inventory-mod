package com.negativeonehero.inventorymod.utils;

import com.negativeonehero.inventorymod.impl.IPlayerInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerUtility {
    @Nullable public static ClientPlayerEntity clientPlayer() {
        return MinecraftClient.getInstance().player;
    }

    @Nullable public static ServerPlayerEntity serverPlayer() {
        if (clientPlayer() != null) {
            return ((IPlayerInventory) clientPlayer().inventory).getServerPlayer();
        }
        return null;
    }
    public static List<PlayerEntity> players() {
        return List.of(clientPlayer(), serverPlayer());
    }
}

/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.command.v2;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.jspecify.annotations.Nullable;

public final class ClientCommands {
    private ClientCommands() {
    }

    public static @Nullable CommandDispatcher<FabricClientCommandSource> getActiveDispatcher() {
        return ClientCommandInternals.getActiveDispatcher();
    }

    public static void refreshCommandCompletions() {
        ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
        if (packetListener == null) {
            throw new IllegalStateException("Not connected to a server (dedicated or integrated)!");
        }
        ClientboundCommandsPacket lastReceivedCommandsPacket = ((ClientCommandInternals.LastReceivedCommandsPacketAccessor)((Object)packetListener)).fabric_api$getLastReceivedCommandsPacket();
        if (lastReceivedCommandsPacket == null) {
            throw new IllegalStateException("Not yet received a 'minecraft:commands' packet!");
        }
        packetListener.handleCommands(lastReceivedCommandsPacket);
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}


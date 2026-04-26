/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.message.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class ServerMessageEvents {
    public static final Event<AllowChatMessage> ALLOW_CHAT_MESSAGE = EventFactory.createArrayBacked(AllowChatMessage.class, handlers -> (message, sender, boundChatType) -> {
        for (AllowChatMessage handler : handlers) {
            if (handler.allowChatMessage(message, sender, boundChatType)) continue;
            return false;
        }
        return true;
    });
    public static final Event<AllowGameMessage> ALLOW_GAME_MESSAGE = EventFactory.createArrayBacked(AllowGameMessage.class, handlers -> (server, message, overlay) -> {
        for (AllowGameMessage handler : handlers) {
            if (handler.allowGameMessage(server, message, overlay)) continue;
            return false;
        }
        return true;
    });
    public static final Event<AllowCommandMessage> ALLOW_COMMAND_MESSAGE = EventFactory.createArrayBacked(AllowCommandMessage.class, handlers -> (message, source, boundChatType) -> {
        for (AllowCommandMessage handler : handlers) {
            if (handler.allowCommandMessage(message, source, boundChatType)) continue;
            return false;
        }
        return true;
    });
    public static final Event<ChatMessage> CHAT_MESSAGE = EventFactory.createArrayBacked(ChatMessage.class, handlers -> (message, sender, boundChatType) -> {
        for (ChatMessage handler : handlers) {
            handler.onChatMessage(message, sender, boundChatType);
        }
    });
    public static final Event<GameMessage> GAME_MESSAGE = EventFactory.createArrayBacked(GameMessage.class, handlers -> (server, message, overlay) -> {
        for (GameMessage handler : handlers) {
            handler.onGameMessage(server, message, overlay);
        }
    });
    public static final Event<CommandMessage> COMMAND_MESSAGE = EventFactory.createArrayBacked(CommandMessage.class, handlers -> (message, source, boundChatType) -> {
        for (CommandMessage handler : handlers) {
            handler.onCommandMessage(message, source, boundChatType);
        }
    });

    private ServerMessageEvents() {
    }

    @FunctionalInterface
    public static interface CommandMessage {
        public void onCommandMessage(PlayerChatMessage var1, CommandSourceStack var2, ChatType.Bound var3);
    }

    @FunctionalInterface
    public static interface GameMessage {
        public void onGameMessage(MinecraftServer var1, Component var2, boolean var3);
    }

    @FunctionalInterface
    public static interface ChatMessage {
        public void onChatMessage(PlayerChatMessage var1, ServerPlayer var2, ChatType.Bound var3);
    }

    @FunctionalInterface
    public static interface AllowCommandMessage {
        public boolean allowCommandMessage(PlayerChatMessage var1, CommandSourceStack var2, ChatType.Bound var3);
    }

    @FunctionalInterface
    public static interface AllowGameMessage {
        public boolean allowGameMessage(MinecraftServer var1, Component var2, boolean var3);
    }

    @FunctionalInterface
    public static interface AllowChatMessage {
        public boolean allowChatMessage(PlayerChatMessage var1, ServerPlayer var2, ChatType.Bound var3);
    }
}


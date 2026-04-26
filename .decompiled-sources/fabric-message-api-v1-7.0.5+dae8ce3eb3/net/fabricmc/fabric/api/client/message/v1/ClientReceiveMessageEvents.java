/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.message.v1;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jspecify.annotations.Nullable;

public final class ClientReceiveMessageEvents {
    public static final Event<AllowChat> ALLOW_CHAT = EventFactory.createArrayBacked(AllowChat.class, listeners -> (message, playerChatMessage, sender, boundChatType, timeStamp) -> {
        boolean allow = true;
        for (AllowChat listener : listeners) {
            allow &= listener.allowReceiveChatMessage(message, playerChatMessage, sender, boundChatType, timeStamp);
        }
        return allow;
    });
    public static final Event<AllowGame> ALLOW_GAME = EventFactory.createArrayBacked(AllowGame.class, listeners -> (message, overlay) -> {
        boolean allow = true;
        for (AllowGame listener : listeners) {
            allow &= listener.allowReceiveGameMessage(message, overlay);
        }
        return allow;
    });
    public static final Event<ModifyGame> MODIFY_GAME = EventFactory.createArrayBacked(ModifyGame.class, listeners -> (message, overlay) -> {
        for (ModifyGame listener : listeners) {
            message = listener.modifyReceivedGameMessage(message, overlay);
        }
        return message;
    });
    public static final Event<Chat> CHAT = EventFactory.createArrayBacked(Chat.class, listeners -> (message, playerChatMessage, sender, boundChatType, timeStamp) -> {
        for (Chat listener : listeners) {
            listener.onReceiveChatMessage(message, playerChatMessage, sender, boundChatType, timeStamp);
        }
    });
    public static final Event<Game> GAME = EventFactory.createArrayBacked(Game.class, listeners -> (message, overlay) -> {
        for (Game listener : listeners) {
            listener.onReceiveGameMessage(message, overlay);
        }
    });
    public static final Event<ChatCanceled> CHAT_CANCELED = EventFactory.createArrayBacked(ChatCanceled.class, listeners -> (message, playerChatMessage, sender, boundChatType, timeStamp) -> {
        for (ChatCanceled listener : listeners) {
            listener.onReceiveChatMessageCanceled(message, playerChatMessage, sender, boundChatType, timeStamp);
        }
    });
    public static final Event<GameCanceled> GAME_CANCELED = EventFactory.createArrayBacked(GameCanceled.class, listeners -> (message, overlay) -> {
        for (GameCanceled listener : listeners) {
            listener.onReceiveGameMessageCanceled(message, overlay);
        }
    });

    private ClientReceiveMessageEvents() {
    }

    @FunctionalInterface
    public static interface GameCanceled {
        public void onReceiveGameMessageCanceled(Component var1, boolean var2);
    }

    @FunctionalInterface
    public static interface ChatCanceled {
        public void onReceiveChatMessageCanceled(Component var1, @Nullable PlayerChatMessage var2, @Nullable GameProfile var3, ChatType.Bound var4, Instant var5);
    }

    @FunctionalInterface
    public static interface Game {
        public void onReceiveGameMessage(Component var1, boolean var2);
    }

    @FunctionalInterface
    public static interface Chat {
        public void onReceiveChatMessage(Component var1, @Nullable PlayerChatMessage var2, @Nullable GameProfile var3, ChatType.Bound var4, Instant var5);
    }

    @FunctionalInterface
    public static interface ModifyGame {
        public Component modifyReceivedGameMessage(Component var1, boolean var2);
    }

    @FunctionalInterface
    public static interface AllowGame {
        public boolean allowReceiveGameMessage(Component var1, boolean var2);
    }

    @FunctionalInterface
    public static interface AllowChat {
        public boolean allowReceiveChatMessage(Component var1, @Nullable PlayerChatMessage var2, @Nullable GameProfile var3, ChatType.Bound var4, Instant var5);
    }
}


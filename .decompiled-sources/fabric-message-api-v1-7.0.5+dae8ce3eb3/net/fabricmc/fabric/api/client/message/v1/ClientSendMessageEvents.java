/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.message.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClientSendMessageEvents {
    public static final Event<AllowChat> ALLOW_CHAT = EventFactory.createArrayBacked(AllowChat.class, listeners -> message -> {
        for (AllowChat listener : listeners) {
            if (listener.allowSendChatMessage(message)) continue;
            return false;
        }
        return true;
    });
    public static final Event<AllowCommand> ALLOW_COMMAND = EventFactory.createArrayBacked(AllowCommand.class, listeners -> command -> {
        for (AllowCommand listener : listeners) {
            if (listener.allowSendCommandMessage(command)) continue;
            return false;
        }
        return true;
    });
    public static final Event<ModifyChat> MODIFY_CHAT = EventFactory.createArrayBacked(ModifyChat.class, listeners -> message -> {
        for (ModifyChat listener : listeners) {
            message = listener.modifySendChatMessage(message);
        }
        return message;
    });
    public static final Event<ModifyCommand> MODIFY_COMMAND = EventFactory.createArrayBacked(ModifyCommand.class, listeners -> command -> {
        for (ModifyCommand listener : listeners) {
            command = listener.modifySendCommandMessage(command);
        }
        return command;
    });
    public static final Event<Chat> CHAT = EventFactory.createArrayBacked(Chat.class, listeners -> message -> {
        for (Chat listener : listeners) {
            listener.onSendChatMessage(message);
        }
    });
    public static final Event<Command> COMMAND = EventFactory.createArrayBacked(Command.class, listeners -> command -> {
        for (Command listener : listeners) {
            listener.onSendCommandMessage(command);
        }
    });
    public static final Event<ChatCanceled> CHAT_CANCELED = EventFactory.createArrayBacked(ChatCanceled.class, listeners -> message -> {
        for (ChatCanceled listener : listeners) {
            listener.onSendChatMessageCanceled(message);
        }
    });
    public static final Event<CommandCanceled> COMMAND_CANCELED = EventFactory.createArrayBacked(CommandCanceled.class, listeners -> command -> {
        for (CommandCanceled listener : listeners) {
            listener.onSendCommandMessageCanceled(command);
        }
    });

    private ClientSendMessageEvents() {
    }

    @FunctionalInterface
    public static interface CommandCanceled {
        public void onSendCommandMessageCanceled(String var1);
    }

    @FunctionalInterface
    public static interface ChatCanceled {
        public void onSendChatMessageCanceled(String var1);
    }

    @FunctionalInterface
    public static interface Command {
        public void onSendCommandMessage(String var1);
    }

    @FunctionalInterface
    public static interface Chat {
        public void onSendChatMessage(String var1);
    }

    @FunctionalInterface
    public static interface ModifyCommand {
        public String modifySendCommandMessage(String var1);
    }

    @FunctionalInterface
    public static interface ModifyChat {
        public String modifySendChatMessage(String var1);
    }

    @FunctionalInterface
    public static interface AllowCommand {
        public boolean allowSendCommandMessage(String var1);
    }

    @FunctionalInterface
    public static interface AllowChat {
        public boolean allowSendChatMessage(String var1);
    }
}


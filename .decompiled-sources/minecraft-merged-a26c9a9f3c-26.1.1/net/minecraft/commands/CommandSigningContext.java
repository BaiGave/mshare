/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands;

import java.util.Map;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jspecify.annotations.Nullable;

public interface CommandSigningContext {
    public static final CommandSigningContext ANONYMOUS = new CommandSigningContext(){

        @Override
        public @Nullable PlayerChatMessage getArgument(String name) {
            return null;
        }
    };

    public @Nullable PlayerChatMessage getArgument(String var1);

    public record SignedArguments(Map<String, PlayerChatMessage> arguments) implements CommandSigningContext
    {
        @Override
        public @Nullable PlayerChatMessage getArgument(String name) {
            return this.arguments.get(name);
        }
    }
}


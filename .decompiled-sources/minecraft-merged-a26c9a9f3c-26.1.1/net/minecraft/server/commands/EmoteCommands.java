/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)Commands.literal("me").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("action", MessageArgument.message()).executes(c -> {
            MessageArgument.resolveChatMessage(c, "action", message -> {
                CommandSourceStack source = (CommandSourceStack)c.getSource();
                PlayerList playerList = source.getServer().getPlayerList();
                playerList.broadcastChatMessage((PlayerChatMessage)message, source, ChatType.bind(ChatType.EMOTE_COMMAND, source));
            });
            return 1;
        })));
    }
}


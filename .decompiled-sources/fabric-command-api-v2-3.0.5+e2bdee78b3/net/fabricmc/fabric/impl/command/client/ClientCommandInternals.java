/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.command.client;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.mixin.command.HelpCommandAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.util.profiling.Profiler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientCommandInternals {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommandInternals.class);
    private static final String API_COMMAND_NAME = "fabric-command-api-v2:client";
    private static final String SHORT_API_COMMAND_NAME = "fcc";
    private static @Nullable CommandDispatcher<FabricClientCommandSource> activeDispatcher;

    public static void setActiveDispatcher(@Nullable CommandDispatcher<FabricClientCommandSource> dispatcher) {
        activeDispatcher = dispatcher;
    }

    public static @Nullable CommandDispatcher<FabricClientCommandSource> getActiveDispatcher() {
        return activeDispatcher;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean executeCommand(String command) {
        Minecraft instance = Minecraft.getInstance();
        FabricClientCommandSource source = (FabricClientCommandSource)((Object)instance.getConnection().getSuggestionsProvider());
        Profiler.get().push(command);
        try {
            activeDispatcher.execute(command, source);
            boolean bl = true;
            return bl;
        }
        catch (CommandSyntaxException e) {
            boolean ignored = ClientCommandInternals.isIgnoredException(e.getType());
            if (ignored) {
                LOGGER.debug("Syntax exception for client-sided command '{}'", (Object)command, (Object)e);
                boolean bl = false;
                return bl;
            }
            LOGGER.warn("Syntax exception for client-sided command '{}'", (Object)command, (Object)e);
            source.sendError(ClientCommandInternals.getErrorMessage(e));
            boolean bl = true;
            return bl;
        }
        catch (Exception e) {
            LOGGER.warn("Error while executing client-sided command '{}'", (Object)command, (Object)e);
            source.sendError(Component.nullToEmpty(e.getMessage()));
            boolean bl = true;
            return bl;
        }
        finally {
            Profiler.get().pop();
        }
    }

    private static boolean isIgnoredException(CommandExceptionType type) {
        BuiltInExceptionProvider builtins = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
        return type == builtins.dispatcherUnknownCommand() || type == builtins.dispatcherParseException();
    }

    private static Component getErrorMessage(CommandSyntaxException e) {
        Component message = ComponentUtils.fromMessage(e.getRawMessage());
        String context = e.getContext();
        return context != null ? Component.translatable("command.context.parse_error", message, e.getCursor(), context) : message;
    }

    public static void finalizeInit() {
        if (!activeDispatcher.getRoot().getChildren().isEmpty()) {
            LiteralArgumentBuilder<FabricClientCommandSource> help = ClientCommands.literal("help");
            help.executes(ClientCommandInternals::executeRootHelp);
            help.then((ArgumentBuilder<FabricClientCommandSource, ?>)ClientCommands.argument("command", StringArgumentType.greedyString()).executes(ClientCommandInternals::executeArgumentHelp));
            LiteralCommandNode<FabricClientCommandSource> mainNode = activeDispatcher.register((LiteralArgumentBuilder)ClientCommands.literal(API_COMMAND_NAME).then(help));
            activeDispatcher.register((LiteralArgumentBuilder)ClientCommands.literal(SHORT_API_COMMAND_NAME).redirect(mainNode));
        }
        activeDispatcher.findAmbiguities((parent, child, sibling, inputs) -> LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", activeDispatcher.getPath(child), activeDispatcher.getPath(sibling), inputs));
    }

    private static int executeRootHelp(CommandContext<FabricClientCommandSource> context) {
        return ClientCommandInternals.executeHelp(activeDispatcher.getRoot(), context);
    }

    private static int executeArgumentHelp(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        ParseResults<FabricClientCommandSource> parseResults = activeDispatcher.parse(StringArgumentType.getString(context, "command"), context.getSource());
        List<ParsedCommandNode<FabricClientCommandSource>> nodes = parseResults.getContext().getNodes();
        if (nodes.isEmpty()) {
            throw HelpCommandAccessor.getFailedException().create();
        }
        return ClientCommandInternals.executeHelp(Iterables.getLast(nodes).getNode(), context);
    }

    private static int executeHelp(CommandNode<FabricClientCommandSource> startNode, CommandContext<FabricClientCommandSource> context) {
        Map<CommandNode<FabricClientCommandSource>, String> commands = activeDispatcher.getSmartUsage(startNode, context.getSource());
        for (String command : commands.values()) {
            context.getSource().sendFeedback(Component.literal("/" + command));
        }
        return commands.size();
    }

    public static void addCommands(CommandDispatcher<FabricClientCommandSource> target, FabricClientCommandSource source) {
        HashMap<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> nodes = new HashMap<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>>();
        nodes.put(activeDispatcher.getRoot(), target.getRoot());
        ClientCommandInternals.copyChildren(activeDispatcher.getRoot(), target.getRoot(), source, nodes);
    }

    private static void copyChildren(CommandNode<FabricClientCommandSource> root, CommandNode<FabricClientCommandSource> newRoot, FabricClientCommandSource source, Map<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> nodes) {
        for (CommandNode<FabricClientCommandSource> child : root.getChildren()) {
            if (!child.canUse(source)) continue;
            ArgumentBuilder<FabricClientCommandSource, ?> builder = child.createBuilder();
            builder.requires(s -> true);
            if (builder.getCommand() != null) {
                builder.executes(context -> 0);
            }
            if (builder.getRedirect() != null) {
                builder.redirect(nodes.get(builder.getRedirect()));
            }
            CommandNode<FabricClientCommandSource> result = builder.build();
            nodes.put(child, result);
            newRoot.addChild(result);
            if (child.getChildren().isEmpty()) continue;
            ClientCommandInternals.copyChildren(child, result, source, nodes);
        }
    }

    public static interface LastReceivedCommandsPacketAccessor {
        public @Nullable ClientboundCommandsPacket fabric_api$getLastReceivedCommandsPacket();
    }
}


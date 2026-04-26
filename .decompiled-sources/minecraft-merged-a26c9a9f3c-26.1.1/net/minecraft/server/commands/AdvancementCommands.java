/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
    private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType(msg -> (Component)msg);
    private static final Dynamic2CommandExceptionType ERROR_CRITERION_NOT_FOUND = new Dynamic2CommandExceptionType((name, criterion) -> Component.translatableEscape("commands.advancement.criterionNotFound", name, criterion));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("advancement").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("grant").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("only").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.GRANT, AdvancementCommands.getAdvancements(c, ResourceKeyArgument.getAdvancement(c, "advancement"), Mode.ONLY)))).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((c, p) -> SharedSuggestionProvider.suggest(ResourceKeyArgument.getAdvancement(c, "advancement").value().criteria().keySet(), p)).executes(c -> AdvancementCommands.performCriterion((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.GRANT, ResourceKeyArgument.getAdvancement(c, "advancement"), StringArgumentType.getString(c, "criterion"))))))).then(Commands.literal("from").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.GRANT, AdvancementCommands.getAdvancements(c, ResourceKeyArgument.getAdvancement(c, "advancement"), Mode.FROM)))))).then(Commands.literal("until").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.GRANT, AdvancementCommands.getAdvancements(c, ResourceKeyArgument.getAdvancement(c, "advancement"), Mode.UNTIL)))))).then(Commands.literal("through").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.GRANT, AdvancementCommands.getAdvancements(c, ResourceKeyArgument.getAdvancement(c, "advancement"), Mode.THROUGH)))))).then(Commands.literal("everything").executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.GRANT, ((CommandSourceStack)c.getSource()).getServer().getAdvancements().getAllAdvancements(), false)))))).then(Commands.literal("revoke").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("only").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements(c, ResourceKeyArgument.getAdvancement(c, "advancement"), Mode.ONLY)))).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((c, p) -> SharedSuggestionProvider.suggest(ResourceKeyArgument.getAdvancement(c, "advancement").value().criteria().keySet(), p)).executes(c -> AdvancementCommands.performCriterion((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.REVOKE, ResourceKeyArgument.getAdvancement(c, "advancement"), StringArgumentType.getString(c, "criterion"))))))).then(Commands.literal("from").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements(c, ResourceKeyArgument.getAdvancement(c, "advancement"), Mode.FROM)))))).then(Commands.literal("until").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements(c, ResourceKeyArgument.getAdvancement(c, "advancement"), Mode.UNTIL)))))).then(Commands.literal("through").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("advancement", ResourceKeyArgument.key(Registries.ADVANCEMENT)).executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.REVOKE, AdvancementCommands.getAdvancements(c, ResourceKeyArgument.getAdvancement(c, "advancement"), Mode.THROUGH)))))).then(Commands.literal("everything").executes(c -> AdvancementCommands.perform((CommandSourceStack)c.getSource(), EntityArgument.getPlayers(c, "targets"), Action.REVOKE, ((CommandSourceStack)c.getSource()).getServer().getAdvancements().getAllAdvancements()))))));
    }

    private static int perform(CommandSourceStack source, Collection<ServerPlayer> players, Action action, Collection<AdvancementHolder> advancements) throws CommandSyntaxException {
        return AdvancementCommands.perform(source, players, action, advancements, true);
    }

    private static int perform(CommandSourceStack source, Collection<ServerPlayer> players, Action action, Collection<AdvancementHolder> advancements, boolean showAdvancements) throws CommandSyntaxException {
        int count = 0;
        for (ServerPlayer player : players) {
            count += action.perform(player, advancements, showAdvancements);
        }
        if (count == 0) {
            if (advancements.size() == 1) {
                if (players.size() == 1) {
                    throw ERROR_NO_ACTION_PERFORMED.create(Component.translatable(action.getKey() + ".one.to.one.failure", Advancement.name(advancements.iterator().next()), players.iterator().next().getDisplayName()));
                }
                throw ERROR_NO_ACTION_PERFORMED.create(Component.translatable(action.getKey() + ".one.to.many.failure", Advancement.name(advancements.iterator().next()), players.size()));
            }
            if (players.size() == 1) {
                throw ERROR_NO_ACTION_PERFORMED.create(Component.translatable(action.getKey() + ".many.to.one.failure", advancements.size(), players.iterator().next().getDisplayName()));
            }
            throw ERROR_NO_ACTION_PERFORMED.create(Component.translatable(action.getKey() + ".many.to.many.failure", advancements.size(), players.size()));
        }
        if (advancements.size() == 1) {
            if (players.size() == 1) {
                source.sendSuccess(() -> Component.translatable(action.getKey() + ".one.to.one.success", Advancement.name((AdvancementHolder)advancements.iterator().next()), ((ServerPlayer)players.iterator().next()).getDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable(action.getKey() + ".one.to.many.success", Advancement.name((AdvancementHolder)advancements.iterator().next()), players.size()), true);
            }
        } else if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable(action.getKey() + ".many.to.one.success", advancements.size(), ((ServerPlayer)players.iterator().next()).getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(action.getKey() + ".many.to.many.success", advancements.size(), players.size()), true);
        }
        return count;
    }

    private static int performCriterion(CommandSourceStack source, Collection<ServerPlayer> players, Action action, AdvancementHolder holder, String criterion) throws CommandSyntaxException {
        int count = 0;
        Advancement advancement = holder.value();
        if (!advancement.criteria().containsKey(criterion)) {
            throw ERROR_CRITERION_NOT_FOUND.create(Advancement.name(holder), criterion);
        }
        for (ServerPlayer player : players) {
            if (!action.performCriterion(player, holder, criterion)) continue;
            ++count;
        }
        if (count == 0) {
            if (players.size() == 1) {
                throw ERROR_NO_ACTION_PERFORMED.create(Component.translatable(action.getKey() + ".criterion.to.one.failure", criterion, Advancement.name(holder), players.iterator().next().getDisplayName()));
            }
            throw ERROR_NO_ACTION_PERFORMED.create(Component.translatable(action.getKey() + ".criterion.to.many.failure", criterion, Advancement.name(holder), players.size()));
        }
        if (players.size() == 1) {
            source.sendSuccess(() -> Component.translatable(action.getKey() + ".criterion.to.one.success", criterion, Advancement.name(holder), ((ServerPlayer)players.iterator().next()).getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(action.getKey() + ".criterion.to.many.success", criterion, Advancement.name(holder), players.size()), true);
        }
        return count;
    }

    private static List<AdvancementHolder> getAdvancements(CommandContext<CommandSourceStack> context, AdvancementHolder target, Mode mode) {
        AdvancementTree advancementTree = context.getSource().getServer().getAdvancements().tree();
        AdvancementNode targetNode = advancementTree.get(target);
        if (targetNode == null) {
            return List.of(target);
        }
        ArrayList<AdvancementHolder> advancements = new ArrayList<AdvancementHolder>();
        if (mode.parents) {
            for (AdvancementNode parent = targetNode.parent(); parent != null; parent = parent.parent()) {
                advancements.add(parent.holder());
            }
        }
        advancements.add(target);
        if (mode.children) {
            AdvancementCommands.addChildren(targetNode, advancements);
        }
        return advancements;
    }

    private static void addChildren(AdvancementNode parent, List<AdvancementHolder> output) {
        for (AdvancementNode child : parent.children()) {
            output.add(child.holder());
            AdvancementCommands.addChildren(child, output);
        }
    }

    private static enum Action {
        GRANT("grant"){

            @Override
            protected boolean perform(ServerPlayer player, AdvancementHolder advancement) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (progress.isDone()) {
                    return false;
                }
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(advancement, criterion);
                }
                return true;
            }

            @Override
            protected boolean performCriterion(ServerPlayer player, AdvancementHolder advancement, String criterion) {
                return player.getAdvancements().award(advancement, criterion);
            }
        }
        ,
        REVOKE("revoke"){

            @Override
            protected boolean perform(ServerPlayer player, AdvancementHolder advancement) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.hasProgress()) {
                    return false;
                }
                for (String criterion : progress.getCompletedCriteria()) {
                    player.getAdvancements().revoke(advancement, criterion);
                }
                return true;
            }

            @Override
            protected boolean performCriterion(ServerPlayer player, AdvancementHolder advancement, String criterion) {
                return player.getAdvancements().revoke(advancement, criterion);
            }
        };

        private final String key;

        private Action(String key) {
            this.key = "commands.advancement." + key;
        }

        public int perform(ServerPlayer player, Iterable<AdvancementHolder> advancements, boolean showAdvancements) {
            int count = 0;
            if (!showAdvancements) {
                player.getAdvancements().flushDirty(player, true);
            }
            for (AdvancementHolder advancement : advancements) {
                if (!this.perform(player, advancement)) continue;
                ++count;
            }
            if (!showAdvancements) {
                player.getAdvancements().flushDirty(player, false);
            }
            return count;
        }

        protected abstract boolean perform(ServerPlayer var1, AdvancementHolder var2);

        protected abstract boolean performCriterion(ServerPlayer var1, AdvancementHolder var2, String var3);

        protected String getKey() {
            return this.key;
        }
    }

    private static enum Mode {
        ONLY(false, false),
        THROUGH(true, true),
        FROM(false, true),
        UNTIL(true, false),
        EVERYTHING(true, true);

        private final boolean parents;
        private final boolean children;

        private Mode(boolean parents, boolean children) {
            this.parents = parents;
            this.children = children;
        }
    }
}


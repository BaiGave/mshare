/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jspecify.annotations.Nullable;

public class ScoreboardCommand {
    private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.add.duplicate"));
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.display.alreadyEmpty"));
    private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.display.alreadySet"));
    private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.players.enable.failed"));
    private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.players.enable.invalid"));
    private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((objective, target) -> Component.translatableEscape("commands.scoreboard.players.get.null", objective, target));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("scoreboard").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("objectives").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("list").executes(c -> ScoreboardCommand.listObjectives((CommandSourceStack)c.getSource())))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", StringArgumentType.word()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("criteria", ObjectiveCriteriaArgument.criteria()).executes(c -> ScoreboardCommand.addObjective((CommandSourceStack)c.getSource(), StringArgumentType.getString(c, "objective"), ObjectiveCriteriaArgument.getCriteria(c, "criteria"), Component.literal(StringArgumentType.getString(c, "objective"))))).then(Commands.argument("displayName", ComponentArgument.textComponent(context)).executes(c -> ScoreboardCommand.addObjective((CommandSourceStack)c.getSource(), StringArgumentType.getString(c, "objective"), ObjectiveCriteriaArgument.getCriteria(c, "criteria"), ComponentArgument.getResolvedComponent(c, "displayName")))))))).then(Commands.literal("modify").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("displayname").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("displayName", ComponentArgument.textComponent(context)).executes(c -> ScoreboardCommand.setDisplayName((CommandSourceStack)c.getSource(), ObjectiveArgument.getObjective(c, "objective"), ComponentArgument.getResolvedComponent(c, "displayName")))))).then(ScoreboardCommand.createRenderTypeModify())).then(Commands.literal("displayautoupdate").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("value", BoolArgumentType.bool()).executes(c -> ScoreboardCommand.setDisplayAutoUpdate((CommandSourceStack)c.getSource(), ObjectiveArgument.getObjective(c, "objective"), BoolArgumentType.getBool(c, "value")))))).then(ScoreboardCommand.addNumberFormats(context, Commands.literal("numberformat"), (c, numberFormat) -> ScoreboardCommand.setObjectiveFormat((CommandSourceStack)c.getSource(), ObjectiveArgument.getObjective(c, "objective"), numberFormat)))))).then(Commands.literal("remove").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).executes(c -> ScoreboardCommand.removeObjective((CommandSourceStack)c.getSource(), ObjectiveArgument.getObjective(c, "objective")))))).then(Commands.literal("setdisplay").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("slot", ScoreboardSlotArgument.displaySlot()).executes(c -> ScoreboardCommand.clearDisplaySlot((CommandSourceStack)c.getSource(), ScoreboardSlotArgument.getDisplaySlot(c, "slot")))).then(Commands.argument("objective", ObjectiveArgument.objective()).executes(c -> ScoreboardCommand.setDisplaySlot((CommandSourceStack)c.getSource(), ScoreboardSlotArgument.getDisplaySlot(c, "slot"), ObjectiveArgument.getObjective(c, "objective")))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("players").then((ArgumentBuilder<CommandSourceStack, ?>)((LiteralArgumentBuilder)Commands.literal("list").executes(c -> ScoreboardCommand.listTrackedPlayers((CommandSourceStack)c.getSource()))).then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes(c -> ScoreboardCommand.listTrackedPlayerScores((CommandSourceStack)c.getSource(), ScoreHolderArgument.getName(c, "target")))))).then(Commands.literal("set").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("score", IntegerArgumentType.integer()).executes(c -> ScoreboardCommand.setScore((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getWritableObjective(c, "objective"), IntegerArgumentType.getInteger(c, "score")))))))).then(Commands.literal("get").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).executes(c -> ScoreboardCommand.getScore((CommandSourceStack)c.getSource(), ScoreHolderArgument.getName(c, "target"), ObjectiveArgument.getObjective(c, "objective"))))))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("score", IntegerArgumentType.integer(0)).executes(c -> ScoreboardCommand.addScore((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getWritableObjective(c, "objective"), IntegerArgumentType.getInteger(c, "score")))))))).then(Commands.literal("remove").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("score", IntegerArgumentType.integer(0)).executes(c -> ScoreboardCommand.removeScore((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getWritableObjective(c, "objective"), IntegerArgumentType.getInteger(c, "score")))))))).then(Commands.literal("reset").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes(c -> ScoreboardCommand.resetScores((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets")))).then(Commands.argument("objective", ObjectiveArgument.objective()).executes(c -> ScoreboardCommand.resetScore((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getObjective(c, "objective"))))))).then(Commands.literal("enable").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("objective", ObjectiveArgument.objective()).suggests((c, p) -> ScoreboardCommand.suggestTriggers((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), p)).executes(c -> ScoreboardCommand.enableTrigger((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getObjective(c, "objective"))))))).then(((LiteralArgumentBuilder)Commands.literal("display").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("name").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("name", ComponentArgument.textComponent(context)).executes(c -> ScoreboardCommand.setScoreDisplay((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getObjective(c, "objective"), ComponentArgument.getResolvedComponent(c, "name"))))).executes(c -> ScoreboardCommand.setScoreDisplay((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getObjective(c, "objective"), null)))))).then(Commands.literal("numberformat").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(ScoreboardCommand.addNumberFormats(context, Commands.argument("objective", ObjectiveArgument.objective()), (c, format) -> ScoreboardCommand.setScoreNumberFormat((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getObjective(c, "objective"), format))))))).then(Commands.literal("operation").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targetObjective", ObjectiveArgument.objective()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("operation", OperationArgument.operation()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("source", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("sourceObjective", ObjectiveArgument.objective()).executes(c -> ScoreboardCommand.performOperation((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "targets"), ObjectiveArgument.getWritableObjective(c, "targetObjective"), OperationArgument.getOperation(c, "operation"), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "source"), ObjectiveArgument.getObjective(c, "sourceObjective")))))))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> addNumberFormats(CommandBuildContext context, ArgumentBuilder<CommandSourceStack, ?> top, NumberFormatCommandExecutor callback) {
        return ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)top.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("blank").executes(c -> callback.run(c, BlankFormat.INSTANCE)))).then(Commands.literal("fixed").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("contents", ComponentArgument.textComponent(context)).executes(c -> {
            Component contents = ComponentArgument.getResolvedComponent(c, "contents");
            return callback.run(c, new FixedFormat(contents));
        })))).then(Commands.literal("styled").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("style", StyleArgument.style(context)).executes(c -> {
            Style style = StyleArgument.getStyle(c, "style");
            return callback.run(c, new StyledFormat(style));
        })))).executes(c -> callback.run(c, null));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRenderTypeModify() {
        LiteralArgumentBuilder<CommandSourceStack> result = Commands.literal("rendertype");
        for (ObjectiveCriteria.RenderType renderType : ObjectiveCriteria.RenderType.values()) {
            result.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal(renderType.getId()).executes(c -> ScoreboardCommand.setRenderType((CommandSourceStack)c.getSource(), ObjectiveArgument.getObjective(c, "objective"), renderType)));
        }
        return result;
    }

    private static CompletableFuture<Suggestions> suggestTriggers(CommandSourceStack source, Collection<ScoreHolder> targets, SuggestionsBuilder builder) {
        ArrayList<String> result = Lists.newArrayList();
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        for (Objective objective : scoreboard.getObjectives()) {
            if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) continue;
            boolean available = false;
            for (ScoreHolder name : targets) {
                ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo(name, objective);
                if (scoreInfo != null && !scoreInfo.isLocked()) continue;
                available = true;
                break;
            }
            if (!available) continue;
            result.add(objective.getName());
        }
        return SharedSuggestionProvider.suggest(result, builder);
    }

    private static int getScore(CommandSourceStack source, ScoreHolder target, Objective objective) throws CommandSyntaxException {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(target, objective);
        if (score == null) {
            throw ERROR_NO_VALUE.create(objective.getName(), target.getFeedbackDisplayName());
        }
        source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.get.success", target.getFeedbackDisplayName(), score.value(), objective.getFormattedDisplayName()), false);
        return score.value();
    }

    private static Component getFirstTargetName(Collection<ScoreHolder> names) {
        return names.iterator().next().getFeedbackDisplayName();
    }

    private static int performOperation(CommandSourceStack source, Collection<ScoreHolder> targets, Objective targetObjective, OperationArgument.Operation operation, Collection<ScoreHolder> sources, Objective sourceObjective) throws CommandSyntaxException {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        int result = 0;
        for (ScoreHolder target : targets) {
            ScoreAccess score = scoreboard.getOrCreatePlayerScore(target, targetObjective);
            for (ScoreHolder from : sources) {
                ScoreAccess sourceScore = scoreboard.getOrCreatePlayerScore(from, sourceObjective);
                operation.apply(score, sourceScore);
            }
            result += score.get();
        }
        if (targets.size() == 1) {
            int finalResult = result;
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.operation.success.single", targetObjective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(targets), finalResult), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.operation.success.multiple", targetObjective.getFormattedDisplayName(), targets.size()), true);
        }
        return result;
    }

    private static int enableTrigger(CommandSourceStack source, Collection<ScoreHolder> names, Objective objective) throws CommandSyntaxException {
        if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
            throw ERROR_NOT_TRIGGER.create();
        }
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        int count = 0;
        for (ScoreHolder name : names) {
            ScoreAccess score = scoreboard.getOrCreatePlayerScore(name, objective);
            if (!score.locked()) continue;
            score.unlock();
            ++count;
        }
        if (count == 0) {
            throw ERROR_TRIGGER_ALREADY_ENABLED.create();
        }
        if (names.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.enable.success.single", objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(names)), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.enable.success.multiple", objective.getFormattedDisplayName(), names.size()), true);
        }
        return count;
    }

    private static int resetScores(CommandSourceStack source, Collection<ScoreHolder> names) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        for (ScoreHolder name : names) {
            scoreboard.resetAllPlayerScores(name);
        }
        if (names.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.single", ScoreboardCommand.getFirstTargetName(names)), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.multiple", names.size()), true);
        }
        return names.size();
    }

    private static int resetScore(CommandSourceStack source, Collection<ScoreHolder> names, Objective objective) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        for (ScoreHolder name : names) {
            scoreboard.resetSinglePlayerScore(name, objective);
        }
        if (names.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.specific.single", objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(names)), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.specific.multiple", objective.getFormattedDisplayName(), names.size()), true);
        }
        return names.size();
    }

    private static int setScore(CommandSourceStack source, Collection<ScoreHolder> names, Objective objective, int value) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        for (ScoreHolder name : names) {
            scoreboard.getOrCreatePlayerScore(name, objective).set(value);
        }
        if (names.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.set.success.single", objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(names), value), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.set.success.multiple", objective.getFormattedDisplayName(), names.size(), value), true);
        }
        return value * names.size();
    }

    private static int setScoreDisplay(CommandSourceStack source, Collection<ScoreHolder> names, Objective objective, @Nullable Component display) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        for (ScoreHolder name : names) {
            scoreboard.getOrCreatePlayerScore(name, objective).display(display);
        }
        if (display == null) {
            if (names.size() == 1) {
                source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.name.clear.success.single", ScoreboardCommand.getFirstTargetName(names), objective.getFormattedDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.name.clear.success.multiple", names.size(), objective.getFormattedDisplayName()), true);
            }
        } else if (names.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.name.set.success.single", display, ScoreboardCommand.getFirstTargetName(names), objective.getFormattedDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.name.set.success.multiple", display, names.size(), objective.getFormattedDisplayName()), true);
        }
        return names.size();
    }

    private static int setScoreNumberFormat(CommandSourceStack source, Collection<ScoreHolder> names, Objective objective, @Nullable NumberFormat numberFormat) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        for (ScoreHolder name : names) {
            scoreboard.getOrCreatePlayerScore(name, objective).numberFormatOverride(numberFormat);
        }
        if (numberFormat == null) {
            if (names.size() == 1) {
                source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.numberFormat.clear.success.single", ScoreboardCommand.getFirstTargetName(names), objective.getFormattedDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.numberFormat.clear.success.multiple", names.size(), objective.getFormattedDisplayName()), true);
            }
        } else if (names.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.numberFormat.set.success.single", ScoreboardCommand.getFirstTargetName(names), objective.getFormattedDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.display.numberFormat.set.success.multiple", names.size(), objective.getFormattedDisplayName()), true);
        }
        return names.size();
    }

    private static int addScore(CommandSourceStack source, Collection<ScoreHolder> names, Objective objective, int value) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        int result = 0;
        for (ScoreHolder name : names) {
            ScoreAccess score = scoreboard.getOrCreatePlayerScore(name, objective);
            score.set(score.get() + value);
            result += score.get();
        }
        if (names.size() == 1) {
            int finalResult = result;
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.add.success.single", value, objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(names), finalResult), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.add.success.multiple", value, objective.getFormattedDisplayName(), names.size()), true);
        }
        return result;
    }

    private static int removeScore(CommandSourceStack source, Collection<ScoreHolder> names, Objective objective, int value) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        int result = 0;
        for (ScoreHolder name : names) {
            ScoreAccess score = scoreboard.getOrCreatePlayerScore(name, objective);
            score.set(score.get() - value);
            result += score.get();
        }
        if (names.size() == 1) {
            int finalResult = result;
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.remove.success.single", value, objective.getFormattedDisplayName(), ScoreboardCommand.getFirstTargetName(names), finalResult), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.remove.success.multiple", value, objective.getFormattedDisplayName(), names.size()), true);
        }
        return result;
    }

    private static int listTrackedPlayers(CommandSourceStack source) {
        Collection<ScoreHolder> entities = source.getServer().getScoreboard().getTrackedPlayers();
        if (entities.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.empty"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.success", entities.size(), ComponentUtils.formatList(entities, ScoreHolder::getFeedbackDisplayName)), false);
        }
        return entities.size();
    }

    private static int listTrackedPlayerScores(CommandSourceStack source, ScoreHolder entity) {
        Object2IntMap<Objective> scores = source.getServer().getScoreboard().listPlayerScores(entity);
        if (scores.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.empty", entity.getFeedbackDisplayName()), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.success", entity.getFeedbackDisplayName(), scores.size()), false);
            Object2IntMaps.fastForEach(scores, entry -> source.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.entry", ((Objective)entry.getKey()).getFormattedDisplayName(), entry.getIntValue()), false));
        }
        return scores.size();
    }

    private static int clearDisplaySlot(CommandSourceStack source, DisplaySlot slot) throws CommandSyntaxException {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        if (scoreboard.getDisplayObjective(slot) == null) {
            throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
        }
        ((Scoreboard)scoreboard).setDisplayObjective(slot, null);
        source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.cleared", slot.getSerializedName()), true);
        return 0;
    }

    private static int setDisplaySlot(CommandSourceStack source, DisplaySlot slot, Objective objective) throws CommandSyntaxException {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        if (scoreboard.getDisplayObjective(slot) == objective) {
            throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
        }
        ((Scoreboard)scoreboard).setDisplayObjective(slot, objective);
        source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.set", slot.getSerializedName(), objective.getDisplayName()), true);
        return 0;
    }

    private static int setDisplayName(CommandSourceStack source, Objective objective, Component displayName) {
        if (!objective.getDisplayName().equals(displayName)) {
            objective.setDisplayName(displayName);
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.displayname", objective.getName(), objective.getFormattedDisplayName()), true);
        }
        return 0;
    }

    private static int setDisplayAutoUpdate(CommandSourceStack source, Objective objective, boolean displayAutoUpdate) {
        if (objective.displayAutoUpdate() != displayAutoUpdate) {
            objective.setDisplayAutoUpdate(displayAutoUpdate);
            if (displayAutoUpdate) {
                source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.displayAutoUpdate.enable", objective.getName(), objective.getFormattedDisplayName()), true);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.displayAutoUpdate.disable", objective.getName(), objective.getFormattedDisplayName()), true);
            }
        }
        return 0;
    }

    private static int setObjectiveFormat(CommandSourceStack source, Objective objective, @Nullable NumberFormat numberFormat) {
        objective.setNumberFormat(numberFormat);
        if (numberFormat != null) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.set", objective.getName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.clear", objective.getName()), true);
        }
        return 0;
    }

    private static int setRenderType(CommandSourceStack source, Objective objective, ObjectiveCriteria.RenderType renderType) {
        if (objective.getRenderType() != renderType) {
            objective.setRenderType(renderType);
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.rendertype", objective.getFormattedDisplayName()), true);
        }
        return 0;
    }

    private static int removeObjective(CommandSourceStack source, Objective objective) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.removeObjective(objective);
        source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.remove.success", objective.getFormattedDisplayName()), true);
        return scoreboard.getObjectives().size();
    }

    private static int addObjective(CommandSourceStack source, String name, ObjectiveCriteria criteria, Component displayName) throws CommandSyntaxException {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        if (scoreboard.getObjective(name) != null) {
            throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
        }
        scoreboard.addObjective(name, criteria, displayName, criteria.getDefaultRenderType(), false, null);
        Objective objective = scoreboard.getObjective(name);
        source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.add.success", objective.getFormattedDisplayName()), true);
        return scoreboard.getObjectives().size();
    }

    private static int listObjectives(CommandSourceStack source) {
        Collection<Objective> objectives = source.getServer().getScoreboard().getObjectives();
        if (objectives.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.empty"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.success", objectives.size(), ComponentUtils.formatList(objectives, Objective::getFormattedDisplayName)), false);
        }
        return objectives.size();
    }

    @FunctionalInterface
    public static interface NumberFormatCommandExecutor {
        public int run(CommandContext<CommandSourceStack> var1, @Nullable NumberFormat var2) throws CommandSyntaxException;
    }
}


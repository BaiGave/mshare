/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

public class TeamCommand {
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EXISTS = new SimpleCommandExceptionType(Component.translatable("commands.team.add.duplicate"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_EMPTY = new SimpleCommandExceptionType(Component.translatable("commands.team.empty.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_NAME = new SimpleCommandExceptionType(Component.translatable("commands.team.option.name.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_COLOR = new SimpleCommandExceptionType(Component.translatable("commands.team.option.color.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.friendlyfire.alreadyEnabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.friendlyfire.alreadyDisabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.seeFriendlyInvisibles.alreadyEnabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.seeFriendlyInvisibles.alreadyDisabled"));
    private static final SimpleCommandExceptionType ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.nametagVisibility.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.deathMessageVisibility.unchanged"));
    private static final SimpleCommandExceptionType ERROR_TEAM_COLLISION_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.team.option.collisionRule.unchanged"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("team").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(((LiteralArgumentBuilder)Commands.literal("list").executes(c -> TeamCommand.listTeams((CommandSourceStack)c.getSource()))).then(Commands.argument("team", TeamArgument.team()).executes(c -> TeamCommand.listMembers((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team")))))).then(Commands.literal("add").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("team", StringArgumentType.word()).executes(c -> TeamCommand.createTeam((CommandSourceStack)c.getSource(), StringArgumentType.getString(c, "team")))).then(Commands.argument("displayName", ComponentArgument.textComponent(context)).executes(c -> TeamCommand.createTeam((CommandSourceStack)c.getSource(), StringArgumentType.getString(c, "team"), ComponentArgument.getResolvedComponent(c, "displayName"))))))).then(Commands.literal("remove").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("team", TeamArgument.team()).executes(c -> TeamCommand.deleteTeam((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team")))))).then(Commands.literal("empty").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("team", TeamArgument.team()).executes(c -> TeamCommand.emptyTeam((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team")))))).then(Commands.literal("join").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("team", TeamArgument.team()).executes(c -> TeamCommand.joinTeam((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Collections.singleton(((CommandSourceStack)c.getSource()).getEntityOrException())))).then(Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes(c -> TeamCommand.joinTeam((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "members"))))))).then(Commands.literal("leave").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("members", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes(c -> TeamCommand.leaveTeam((CommandSourceStack)c.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(c, "members")))))).then(Commands.literal("modify").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("team", TeamArgument.team()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("displayName").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("displayName", ComponentArgument.textComponent(context)).executes(c -> TeamCommand.setDisplayName((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), ComponentArgument.getResolvedComponent(c, "displayName")))))).then(Commands.literal("color").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("value", ColorArgument.color()).executes(c -> TeamCommand.setColor((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), ColorArgument.getColor(c, "value")))))).then(Commands.literal("friendlyFire").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("allowed", BoolArgumentType.bool()).executes(c -> TeamCommand.setFriendlyFire((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), BoolArgumentType.getBool(c, "allowed")))))).then(Commands.literal("seeFriendlyInvisibles").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("allowed", BoolArgumentType.bool()).executes(c -> TeamCommand.setFriendlySight((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), BoolArgumentType.getBool(c, "allowed")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("nametagVisibility").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("never").executes(c -> TeamCommand.setNametagVisibility((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.Visibility.NEVER)))).then(Commands.literal("hideForOtherTeams").executes(c -> TeamCommand.setNametagVisibility((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS)))).then(Commands.literal("hideForOwnTeam").executes(c -> TeamCommand.setNametagVisibility((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM)))).then(Commands.literal("always").executes(c -> TeamCommand.setNametagVisibility((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.Visibility.ALWAYS))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("deathMessageVisibility").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("never").executes(c -> TeamCommand.setDeathMessageVisibility((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.Visibility.NEVER)))).then(Commands.literal("hideForOtherTeams").executes(c -> TeamCommand.setDeathMessageVisibility((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.Visibility.HIDE_FOR_OTHER_TEAMS)))).then(Commands.literal("hideForOwnTeam").executes(c -> TeamCommand.setDeathMessageVisibility((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.Visibility.HIDE_FOR_OWN_TEAM)))).then(Commands.literal("always").executes(c -> TeamCommand.setDeathMessageVisibility((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.Visibility.ALWAYS))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("collisionRule").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("never").executes(c -> TeamCommand.setCollision((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.CollisionRule.NEVER)))).then(Commands.literal("pushOwnTeam").executes(c -> TeamCommand.setCollision((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.CollisionRule.PUSH_OWN_TEAM)))).then(Commands.literal("pushOtherTeams").executes(c -> TeamCommand.setCollision((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.CollisionRule.PUSH_OTHER_TEAMS)))).then(Commands.literal("always").executes(c -> TeamCommand.setCollision((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), Team.CollisionRule.ALWAYS))))).then(Commands.literal("prefix").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("prefix", ComponentArgument.textComponent(context)).executes(c -> TeamCommand.setPrefix((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), ComponentArgument.getResolvedComponent(c, "prefix")))))).then(Commands.literal("suffix").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("suffix", ComponentArgument.textComponent(context)).executes(c -> TeamCommand.setSuffix((CommandSourceStack)c.getSource(), TeamArgument.getTeam(c, "team"), ComponentArgument.getResolvedComponent(c, "suffix"))))))));
    }

    private static Component getFirstMemberName(Collection<ScoreHolder> members) {
        return members.iterator().next().getFeedbackDisplayName();
    }

    private static int leaveTeam(CommandSourceStack source, Collection<ScoreHolder> members) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        for (ScoreHolder member : members) {
            scoreboard.removePlayerFromTeam(member.getScoreboardName());
        }
        if (members.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.team.leave.success.single", TeamCommand.getFirstMemberName(members)), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.team.leave.success.multiple", members.size()), true);
        }
        return members.size();
    }

    private static int joinTeam(CommandSourceStack source, PlayerTeam team, Collection<ScoreHolder> members) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        for (ScoreHolder member : members) {
            ((Scoreboard)scoreboard).addPlayerToTeam(member.getScoreboardName(), team);
        }
        if (members.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.team.join.success.single", TeamCommand.getFirstMemberName(members), team.getFormattedDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.team.join.success.multiple", members.size(), team.getFormattedDisplayName()), true);
        }
        return members.size();
    }

    private static int setNametagVisibility(CommandSourceStack source, PlayerTeam team, Team.Visibility visibility) throws CommandSyntaxException {
        if (team.getNameTagVisibility() == visibility) {
            throw ERROR_TEAM_NAMETAG_VISIBLITY_UNCHANGED.create();
        }
        team.setNameTagVisibility(visibility);
        source.sendSuccess(() -> Component.translatable("commands.team.option.nametagVisibility.success", team.getFormattedDisplayName(), visibility.getDisplayName()), true);
        return 0;
    }

    private static int setDeathMessageVisibility(CommandSourceStack source, PlayerTeam team, Team.Visibility visibility) throws CommandSyntaxException {
        if (team.getDeathMessageVisibility() == visibility) {
            throw ERROR_TEAM_DEATH_MESSAGE_VISIBLITY_UNCHANGED.create();
        }
        team.setDeathMessageVisibility(visibility);
        source.sendSuccess(() -> Component.translatable("commands.team.option.deathMessageVisibility.success", team.getFormattedDisplayName(), visibility.getDisplayName()), true);
        return 0;
    }

    private static int setCollision(CommandSourceStack source, PlayerTeam team, Team.CollisionRule collision) throws CommandSyntaxException {
        if (team.getCollisionRule() == collision) {
            throw ERROR_TEAM_COLLISION_UNCHANGED.create();
        }
        team.setCollisionRule(collision);
        source.sendSuccess(() -> Component.translatable("commands.team.option.collisionRule.success", team.getFormattedDisplayName(), collision.getDisplayName()), true);
        return 0;
    }

    private static int setFriendlySight(CommandSourceStack source, PlayerTeam team, boolean allowed) throws CommandSyntaxException {
        if (team.canSeeFriendlyInvisibles() == allowed) {
            if (allowed) {
                throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_ENABLED.create();
            }
            throw ERROR_TEAM_ALREADY_FRIENDLYINVISIBLES_DISABLED.create();
        }
        team.setSeeFriendlyInvisibles(allowed);
        source.sendSuccess(() -> Component.translatable("commands.team.option.seeFriendlyInvisibles." + (allowed ? "enabled" : "disabled"), team.getFormattedDisplayName()), true);
        return 0;
    }

    private static int setFriendlyFire(CommandSourceStack source, PlayerTeam team, boolean allowed) throws CommandSyntaxException {
        if (team.isAllowFriendlyFire() == allowed) {
            if (allowed) {
                throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_ENABLED.create();
            }
            throw ERROR_TEAM_ALREADY_FRIENDLYFIRE_DISABLED.create();
        }
        team.setAllowFriendlyFire(allowed);
        source.sendSuccess(() -> Component.translatable("commands.team.option.friendlyfire." + (allowed ? "enabled" : "disabled"), team.getFormattedDisplayName()), true);
        return 0;
    }

    private static int setDisplayName(CommandSourceStack source, PlayerTeam team, Component displayName) throws CommandSyntaxException {
        if (team.getDisplayName().equals(displayName)) {
            throw ERROR_TEAM_ALREADY_NAME.create();
        }
        team.setDisplayName(displayName);
        source.sendSuccess(() -> Component.translatable("commands.team.option.name.success", team.getFormattedDisplayName()), true);
        return 0;
    }

    private static int setColor(CommandSourceStack source, PlayerTeam team, ChatFormatting color) throws CommandSyntaxException {
        if (team.getColor() == color) {
            throw ERROR_TEAM_ALREADY_COLOR.create();
        }
        team.setColor(color);
        source.sendSuccess(() -> Component.translatable("commands.team.option.color.success", team.getFormattedDisplayName(), color.getName()), true);
        return 0;
    }

    private static int emptyTeam(CommandSourceStack source, PlayerTeam team) throws CommandSyntaxException {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        ArrayList<String> members = Lists.newArrayList(team.getPlayers());
        if (members.isEmpty()) {
            throw ERROR_TEAM_ALREADY_EMPTY.create();
        }
        for (String member : members) {
            ((Scoreboard)scoreboard).removePlayerFromTeam(member, team);
        }
        source.sendSuccess(() -> Component.translatable("commands.team.empty.success", members.size(), team.getFormattedDisplayName()), true);
        return members.size();
    }

    private static int deleteTeam(CommandSourceStack source, PlayerTeam team) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        scoreboard.removePlayerTeam(team);
        source.sendSuccess(() -> Component.translatable("commands.team.remove.success", team.getFormattedDisplayName()), true);
        return scoreboard.getPlayerTeams().size();
    }

    private static int createTeam(CommandSourceStack source, String name) throws CommandSyntaxException {
        return TeamCommand.createTeam(source, name, Component.literal(name));
    }

    private static int createTeam(CommandSourceStack source, String name, Component displayName) throws CommandSyntaxException {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        if (scoreboard.getPlayerTeam(name) != null) {
            throw ERROR_TEAM_ALREADY_EXISTS.create();
        }
        PlayerTeam team = scoreboard.addPlayerTeam(name);
        team.setDisplayName(displayName);
        source.sendSuccess(() -> Component.translatable("commands.team.add.success", team.getFormattedDisplayName()), true);
        return scoreboard.getPlayerTeams().size();
    }

    private static int listMembers(CommandSourceStack source, PlayerTeam team) {
        Collection<String> members = team.getPlayers();
        if (members.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.team.list.members.empty", team.getFormattedDisplayName()), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.team.list.members.success", team.getFormattedDisplayName(), members.size(), ComponentUtils.formatList(members)), false);
        }
        return members.size();
    }

    private static int listTeams(CommandSourceStack source) {
        Collection<PlayerTeam> teams = source.getServer().getScoreboard().getPlayerTeams();
        if (teams.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.team.list.teams.empty"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.team.list.teams.success", teams.size(), ComponentUtils.formatList(teams, PlayerTeam::getFormattedDisplayName)), false);
        }
        return teams.size();
    }

    private static int setPrefix(CommandSourceStack source, PlayerTeam team, Component prefix) {
        team.setPlayerPrefix(prefix);
        source.sendSuccess(() -> Component.translatable("commands.team.option.prefix.success", prefix), false);
        return 1;
    }

    private static int setSuffix(CommandSourceStack source, PlayerTeam team, Component suffix) {
        team.setPlayerSuffix(suffix);
        source.sendSuccess(() -> Component.translatable("commands.team.option.suffix.success", suffix), false);
        return 1;
    }
}


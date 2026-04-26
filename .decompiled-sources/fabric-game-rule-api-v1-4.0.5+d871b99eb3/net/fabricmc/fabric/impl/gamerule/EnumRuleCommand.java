/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gamerule;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.mixin.gamerule.GameRuleCommandAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRule;

public final class EnumRuleCommand {
    public static <E extends Enum<E>> void register(LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder, GameRule<E> enumRule) {
        String name = enumRule.toString();
        literalArgumentBuilder.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal(name).executes(context -> GameRuleCommandAccessor.callQueryRule((CommandSourceStack)context.getSource(), enumRule)));
        CommandNode ruleNode = Commands.literal(name).build();
        for (Enum supportedValue : ((RuleTypeExtensions)((Object)enumRule)).fabric_getSupportedEnumValues()) {
            ruleNode.addChild(((LiteralArgumentBuilder)Commands.literal(supportedValue.toString()).executes(context -> EnumRuleCommand.executeAndSetEnum(context, supportedValue, enumRule))).build());
        }
        literalArgumentBuilder.then(ruleNode);
    }

    public static <E extends Enum<E>> int executeAndSetEnum(CommandContext<CommandSourceStack> context, E value, GameRule<E> enumRule) throws CommandSyntaxException {
        CommandSourceStack commandSourceStack = context.getSource();
        try {
            commandSourceStack.getLevel().getGameRules().set(enumRule, value, commandSourceStack.getServer());
        }
        catch (IllegalArgumentException e) {
            throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
        }
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.gamerule.set", enumRule.id(), enumRule.serialize(value)), true);
        return enumRule.getCommandResult(value);
    }
}


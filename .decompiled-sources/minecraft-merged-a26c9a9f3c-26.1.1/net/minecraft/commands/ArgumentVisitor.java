/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public class ArgumentVisitor {
    public static <S> void visitArguments(ParseResults<S> command, Output<S> output, boolean rejectRootRedirects) {
        CommandContextBuilder<S> child;
        CommandContextBuilder<S> rootContext;
        CommandContextBuilder<S> context = rootContext = command.getContext();
        ArgumentVisitor.visitNodeArguments(context, output);
        while (!((child = context.getChild()) == null || rejectRootRedirects && child.getRootNode() == rootContext.getRootNode())) {
            ArgumentVisitor.visitNodeArguments(child, output);
            context = child;
        }
    }

    private static <S> void visitNodeArguments(CommandContextBuilder<S> context, Output<S> output) {
        Map<String, ParsedArgument<S, ?>> values = context.getArguments();
        for (ParsedCommandNode<S> node : context.getNodes()) {
            CommandNode<S> commandNode = node.getNode();
            if (!(commandNode instanceof ArgumentCommandNode)) continue;
            ArgumentCommandNode argument = (ArgumentCommandNode)commandNode;
            ParsedArgument<S, ?> value = values.get(argument.getName());
            ArgumentVisitor.callVisitor(context, output, argument, value);
        }
    }

    private static <S, T> void callVisitor(CommandContextBuilder<S> context, Output<S> output, ArgumentCommandNode<S, T> argument, @Nullable ParsedArgument<S, ?> value) {
        output.accept(context, argument, value);
    }

    @FunctionalInterface
    public static interface Output<S> {
        public <T> void accept(CommandContextBuilder<S> var1, ArgumentCommandNode<S, T> var2, @Nullable ParsedArgument<S, T> var3);
    }
}


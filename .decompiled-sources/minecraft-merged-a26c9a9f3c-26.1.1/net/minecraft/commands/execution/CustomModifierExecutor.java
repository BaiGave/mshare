/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.execution;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionControl;

public interface CustomModifierExecutor<T> {
    public void apply(T var1, List<T> var2, ContextChain<T> var3, ChainModifiers var4, ExecutionControl<T> var5);

    public static interface ModifierAdapter<T>
    extends CustomModifierExecutor<T>,
    RedirectModifier<T> {
        @Override
        default public Collection<T> apply(CommandContext<T> context) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}


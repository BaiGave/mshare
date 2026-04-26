/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.entity.event.effect;

import java.util.Stack;
import net.fabricmc.fabric.api.entity.event.v1.effect.EffectEventContext;
import net.fabricmc.fabric.impl.entity.event.effect.EffectEventContextImpl;

public final class MobEffectUtil {
    private static final ThreadLocal<Stack<EffectEventContext>> CURRENT_COMMAND_CONTEXT = ThreadLocal.withInitial(() -> {
        Stack<EffectEventContext> stack = new Stack<EffectEventContext>();
        stack.push(EffectEventContextImpl.DEFAULT);
        return stack;
    });

    private MobEffectUtil() {
    }

    public static EffectEventContext getCommandContext() {
        return (EffectEventContext)CURRENT_COMMAND_CONTEXT.get().getLast();
    }

    public static void pushContext(EffectEventContext context) {
        CURRENT_COMMAND_CONTEXT.get().push(context);
    }

    public static void popContext() {
        CURRENT_COMMAND_CONTEXT.get().pop();
    }
}


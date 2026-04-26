/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event.effect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.ResultConsumer;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.impl.entity.event.effect.EffectEventContextImpl;
import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={ContextChain.class}, remap=false)
public final class ContextChainMixin {
    private ContextChainMixin() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"runExecutable"}, remap=false)
    private static <S> int onRunExecutable(CommandContext<S> executable, S source, ResultConsumer<S> resultConsumer, boolean forkedMode, Operation<Integer> original) {
        int result;
        CommandNode<S> commandNode = executable.getNodes().getFirst().getNode();
        if (!(commandNode instanceof LiteralCommandNode)) {
            return original.call(executable, source, resultConsumer, forkedMode);
        }
        LiteralCommandNode commandNode2 = (LiteralCommandNode)commandNode;
        try {
            MobEffectUtil.pushContext(new EffectEventContextImpl(true, commandNode2.getName()));
            result = original.call(executable, source, resultConsumer, forkedMode);
        }
        finally {
            MobEffectUtil.popContext();
        }
        return result;
    }
}


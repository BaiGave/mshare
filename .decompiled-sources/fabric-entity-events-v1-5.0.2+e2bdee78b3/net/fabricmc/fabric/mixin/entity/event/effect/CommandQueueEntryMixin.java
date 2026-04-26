/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event.effect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.impl.entity.event.effect.EffectEventContextImpl;
import net.fabricmc.fabric.impl.entity.event.effect.MobEffectUtil;
import net.fabricmc.fabric.mixin.entity.event.effect.BuildContextsAccessor;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.tasks.BuildContexts;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={CommandQueueEntry.class})
public final class CommandQueueEntryMixin<T extends ExecutionCommandSource<T>, S> {
    @Shadow
    @Final
    private EntryAction<T> action;

    private CommandQueueEntryMixin() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"execute"})
    private void onExecute(ExecutionContext<T> executionContext, Operation<Void> original) {
        EntryAction<T> entryAction = this.action;
        if (!(entryAction instanceof BuildContexts.TopLevel)) {
            original.call(executionContext);
            return;
        }
        BuildContexts.TopLevel topLevel = (BuildContexts.TopLevel)entryAction;
        CommandNode commandNode = ((BuildContextsAccessor)((Object)topLevel)).getCommand().getTopContext().getNodes().getFirst().getNode();
        if (!(commandNode instanceof LiteralCommandNode)) {
            original.call(executionContext);
            return;
        }
        LiteralCommandNode commandNode2 = (LiteralCommandNode)commandNode;
        try {
            MobEffectUtil.pushContext(new EffectEventContextImpl(true, commandNode2.getName()));
            original.call(executionContext);
        }
        finally {
            MobEffectUtil.popContext();
        }
    }
}


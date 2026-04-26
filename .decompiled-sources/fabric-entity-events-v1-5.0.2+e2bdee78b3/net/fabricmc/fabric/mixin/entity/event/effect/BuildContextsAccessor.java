/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event.effect;

import com.mojang.brigadier.context.ContextChain;
import net.minecraft.commands.execution.tasks.BuildContexts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BuildContexts.class})
public interface BuildContextsAccessor<S> {
    @Accessor
    public ContextChain<S> getCommand();
}


/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.lifecycle;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Options.class})
public interface OptionsAccessor {
    @Invoker
    public void invokeProcessOptions(Options.FieldAccess var1);
}


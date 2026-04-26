/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.input;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={MouseHandler.class})
public interface MouseHandlerAccessor {
    @Invoker
    public void invokeOnButton(long var1, MouseButtonInfo var3, int var4);

    @Invoker
    public void invokeOnScroll(long var1, double var3, double var5);

    @Invoker
    public void invokeOnMove(long var1, double var3, double var5);
}


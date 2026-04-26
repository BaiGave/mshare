/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.input;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={KeyboardHandler.class})
public interface KeyboardHandlerAccessor {
    @Invoker
    public void invokeKeyPress(long var1, int var3, KeyEvent var4);

    @Invoker
    public void invokeCharTyped(long var1, CharacterEvent var3);
}


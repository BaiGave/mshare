/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.threading;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Main.class})
public class MainMixin {
    @WrapWithCondition(method={"main"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/Util;startTimerHackThread()V")})
    private static boolean dontStartAnotherTimerHack() {
        return false;
    }
}


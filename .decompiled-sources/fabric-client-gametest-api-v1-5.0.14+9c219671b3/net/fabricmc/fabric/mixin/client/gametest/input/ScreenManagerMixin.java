/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.input;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ScreenManager.class})
public class ScreenManagerMixin {
    @ModifyExpressionValue(method={"findBestMonitor(Lcom/mojang/blaze3d/platform/Window;)Lcom/mojang/blaze3d/platform/Monitor;"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/platform/Window;getScreenWidth()I")})
    private int getRealWidth(int original, Window window) {
        return ((WindowHooks)((Object)window)).fabric_getRealWidth();
    }

    @ModifyExpressionValue(method={"findBestMonitor(Lcom/mojang/blaze3d/platform/Window;)Lcom/mojang/blaze3d/platform/Monitor;"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/platform/Window;getScreenHeight()I")})
    private int getRealHeight(int original, Window window) {
        return ((WindowHooks)((Object)window)).fabric_getRealHeight();
    }
}


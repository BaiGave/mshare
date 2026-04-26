/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.input;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Minecraft.class})
public class MinecraftMixin {
    @Shadow
    @Final
    private Window window;

    @ModifyExpressionValue(method={"renderFrame"}, at={@At(value="FIELD", target="Lnet/minecraft/client/renderer/state/WindowRenderState;isMinimized:Z", opcode=180)})
    private boolean hasZeroRealWidthOrHeight(boolean original) {
        WindowHooks windowHooks = (WindowHooks)((Object)this.window);
        return windowHooks.fabric_getRealFramebufferWidth() == 0 || windowHooks.fabric_getRealFramebufferHeight() == 0;
    }
}


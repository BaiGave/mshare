/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.input;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.fabric.impl.client.gametest.util.WindowHooks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"com.mojang.blaze3d.opengl.GlCommandEncoder"})
public class GlCommandEncoderMixin {
    @WrapOperation(method={"presentTexture"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V")})
    private void blitFrameBuffer(DirectStateAccess manager, int readFramebuffer, int drawFramebuffer, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter, Operation<Void> original, @Local(argsOnly=true) GpuTextureView gpuTextureView) {
        if (gpuTextureView.texture() == Minecraft.getInstance().getMainRenderTarget().getColorTexture()) {
            WindowHooks window = (WindowHooks)((Object)Minecraft.getInstance().getWindow());
            dstX1 = window.fabric_getRealFramebufferWidth();
            dstY1 = window.fabric_getRealFramebufferHeight();
        }
        original.call(manager, readFramebuffer, drawFramebuffer, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }
}


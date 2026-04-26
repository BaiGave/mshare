package com.mshare.screen.mixin;

import com.mshare.screen.ScreenCaptureManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into Minecraft client initialization to:
 * - Start the screen capture system
 * - Store the mainRenderTarget reference for screen capture
 */
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
        at = @At("TAIL"),
        method = "<init>"
    )
    private void onInit(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        RenderTarget renderTarget = mc.getMainRenderTarget();
        ScreenCaptureManager.setMainRenderTarget(renderTarget);

        com.mshare.screen.ScreenCaptureManager manager = com.mshare.screen.ScreenCaptureManager.getInstance();
        if (manager.init()) {
            manager.getClass();
        }
    }
}

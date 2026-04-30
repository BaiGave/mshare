package com.mshare.screen.mixin;

import com.mshare.screen.ScreenCaptureManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into RenderSystem.flipFrame().
 *
 * At TAIL: capture screen (after presentFrame, FBO 0 is restored and ready for glReadPixels)
 */
@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    /**
     * At TAIL of flipFrame:
     * - Capture screen AFTER presentFrame (framebuffer is restored to known state)
     */
    @Inject(at = @At("TAIL"), method = "flipFrame")
    private static void onFlipFrameTail(CallbackInfo ci) {
        // Capture screen AFTER presentFrame — the default framebuffer (FBO 0)
        // is now restored and ready for glReadPixels.
        // This must be at TAIL, not HEAD, because presentFrame() changes FBO state.
        ScreenCaptureManager.getInstance().onFrameFlip();
    }
}

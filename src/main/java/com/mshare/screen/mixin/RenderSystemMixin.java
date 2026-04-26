package com.mshare.screen.mixin;

import com.mshare.screen.MeshCaptureManager;
import com.mshare.screen.ScreenCaptureManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into RenderSystem.flipFrame().
 *
 * - HEAD: beginFrame (reset mesh collector)
 * - TAIL: endFrame (merge worker buffers + write to shared memory)
 *         + capture screen (after presentFrame, FBO 0 is restored and ready for glReadPixels)
 */
@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    /**
     * At HEAD of flipFrame:
     * - Begin mesh frame (clears all worker buffers)
     */
    @Inject(at = @At("HEAD"), method = "flipFrame")
    private static void onFlipFrameHead(CallbackInfo ci) {
        MeshCaptureManager meshManager = MeshCaptureManager.getInstance();
        if (meshManager.isEnabled()) {
            meshManager.beginFrame();
        }
    }

    /**
     * At TAIL of flipFrame:
     * - End mesh frame (merge worker buffers + write to shared memory)
     * - Capture screen frame AFTER presentFrame (framebuffer is restored to known state)
     */
    @Inject(at = @At("TAIL"), method = "flipFrame")
    private static void onFlipFrameTail(CallbackInfo ci) {
        MeshCaptureManager meshManager = MeshCaptureManager.getInstance();
        if (meshManager.isEnabled()) {
            meshManager.endFrame();
        }

        // Capture screen AFTER presentFrame — the default framebuffer (FBO 0)
        // is now restored and ready for glReadPixels.
        // This must be at TAIL, not HEAD, because presentFrame() changes FBO state.
        ScreenCaptureManager.getInstance().onFrameFlip();
    }
}

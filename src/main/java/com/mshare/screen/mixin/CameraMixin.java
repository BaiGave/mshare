package com.mshare.screen.mixin;

import com.mshare.screen.CameraDataWriter;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to capture camera updates.
 * Injects into Camera.update() to write camera data to shared memory.
 */
@Mixin(Camera.class)
public class CameraMixin {

    @Inject(
        at = @At("TAIL"),
        method = "update"
    )
    private void onCameraUpdate(CallbackInfo ci) {
        // Write camera data to shared memory
        Camera camera = (Camera) (Object) this;
        CameraDataWriter.getInstance().writeIfNeeded(camera);
    }
}

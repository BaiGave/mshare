package com.mshare;

import net.fabricmc.api.ClientModInitializer;
import com.mshare.screen.ScreenCaptureManager;
import com.mshare.screen.CameraDataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side mod initialization.
 * Runs on the client only (not on dedicated server).
 */
public class MshareModClient implements ClientModInitializer {
    public static final String MOD_ID = "mshare";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID + "-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("MshareMod client initialized");

        // Register keybind for toggling screen capture
        // This could be expanded to include a GUI config screen
        ScreenCaptureManager.getInstance();
        LOGGER.info("Screen capture system ready");

        // Initialize camera data capture
        CameraDataWriter.getInstance().init();
        LOGGER.info("Camera data capture system ready");
    }
}

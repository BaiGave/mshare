package com.mshare.screen;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central manager for screen capture operations.
 * Handles initialization, per-frame capture requests, and cleanup.
 */
public final class ScreenCaptureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenCaptureManager.class);

    private static ScreenCaptureManager instance;
    private static RenderTarget mainRenderTarget;

    private ScreenCapture screenCapture;
    private boolean initialized = false;
    private boolean enabled = true;
    private int targetFps = 30;
    private int downscaleFactor = 1;

    private ScreenCaptureManager() {}

    private static final java.util.concurrent.atomic.AtomicBoolean SHUTDOWN_HOOK_REGISTERED = new java.util.concurrent.atomic.AtomicBoolean(false);

    public static synchronized ScreenCaptureManager getInstance() {
        if (instance == null) {
            instance = new ScreenCaptureManager();
            if (SHUTDOWN_HOOK_REGISTERED.compareAndSet(false, true)) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (instance != null) {
                        instance.shutdown();
                    }
                }, "ScreenCaptureManager-Shutdown"));
            }
        }
        return instance;
    }

    /**
     * Set the main render target for screen capture.
     * Must be called before init().
     */
    public static void setMainRenderTarget(RenderTarget target) {
        mainRenderTarget = target;
        LOGGER.info("Main render target set for screen capture");
    }

    /**
     * Get the main render target.
     */
    public static RenderTarget getMainRenderTarget() {
        return mainRenderTarget;
    }

    /**
     * Initialize the screen capture system.
     */
    public boolean init() {
        if (initialized) {
            LOGGER.warn("ScreenCaptureManager already initialized");
            return true;
        }

        if (mainRenderTarget == null) {
            LOGGER.error("Cannot init ScreenCaptureManager: mainRenderTarget is null");
            return false;
        }

        try {
            // Create screen capture (mainRenderTarget is accessed via ScreenCaptureManager)
            screenCapture = ScreenCapture.create();
            screenCapture.setTargetFps(targetFps);
            screenCapture.setDownscaleFactor(downscaleFactor);

            if (!screenCapture.init()) {
                LOGGER.error("Failed to initialize screen capture");
                return false;
            }

            initialized = true;
            LOGGER.info("ScreenCaptureManager initialized successfully");
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to initialize ScreenCaptureManager", e);
            return false;
        }
    }

    /**
     * Called each frame from RenderSystemMixin.
     */
    public void onFrameFlip() {
        if (!initialized || !enabled || screenCapture == null) {
            return;
        }

        try {
            screenCapture.captureIfNeeded();
        } catch (Exception e) {
            LOGGER.error("Error during frame capture", e);
        }
    }

    /**
     * Enable or disable screen capture.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LOGGER.info("Screen capture {}", enabled ? "enabled" : "disabled");
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the target capture frame rate.
     */
    public void setTargetFps(int fps) {
        this.targetFps = Math.max(1, Math.min(fps, 60));
        if (screenCapture != null) {
            screenCapture.setTargetFps(this.targetFps);
        }
    }

    public int getTargetFps() {
        return targetFps;
    }

    /**
     * Set the downscale factor (1 = full resolution, 2 = half, 4 = quarter).
     */
    public void setDownscaleFactor(int factor) {
        this.downscaleFactor = Math.max(1, Math.min(factor, 8));
        if (screenCapture != null) {
            screenCapture.setDownscaleFactor(this.downscaleFactor);
        }
    }

    public int getDownscaleFactor() {
        return downscaleFactor;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public int getFrameCount() {
        return screenCapture != null ? screenCapture.getFrameCount() : 0;
    }

    /**
     * Cleanup resources.
     */
    public void shutdown() {
        LOGGER.info("Shutting down ScreenCaptureManager");
        if (screenCapture != null) {
            screenCapture.shutdown();
            screenCapture = null;
        }
        initialized = false;
    }
}

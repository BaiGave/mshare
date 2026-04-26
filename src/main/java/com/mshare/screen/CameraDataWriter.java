package com.mshare.screen;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camera data capture manager.
 * Writes camera data to shared memory for external processes.
 */
public final class CameraDataWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraDataWriter.class);

    private static CameraDataWriter instance;

    private CameraSharedMemory sharedMemory;
    private boolean initialized = false;
    private boolean enabled = true;

    // Throttle: write at most every N nanoseconds
    private long writeIntervalNs = 33_333_333L; // ~30 FPS
    private long lastWriteTime = 0;

    private CameraDataWriter() {}

    public static synchronized CameraDataWriter getInstance() {
        if (instance == null) {
            instance = new CameraDataWriter();
        }
        return instance;
    }

    /**
     * Initialize the camera data writer.
     */
    public boolean init() {
        if (initialized) {
            return true;
        }

        try {
            sharedMemory = CameraSharedMemory.createServer(CameraDataHeader.SIZE);
            initialized = true;
            LOGGER.info("CameraDataWriter initialized successfully");
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to initialize CameraDataWriter", e);
            return false;
        }
    }

    /**
     * Write camera data if enough time has passed since last write.
     */
    public void writeIfNeeded(Camera camera) {
        if (!initialized || !enabled || sharedMemory == null) {
            return;
        }

        long now = System.nanoTime();
        if (now - lastWriteTime < writeIntervalNs) {
            return;
        }

        writeCameraData(camera);
        lastWriteTime = now;
    }

    /**
     * Force write camera data immediately.
     */
    public void writeCameraData(Camera camera) {
        if (sharedMemory == null) {
            return;
        }

        try {
            // Get camera data from Minecraft Camera class
            var position = camera.position();
            float xRot = camera.xRot();
            float yRot = camera.yRot();
            Quaternionf rotation = camera.rotation();
            float fov = camera.getFov();

            // Determine camera type
            int cameraType = CameraDataHeader.CAMERA_FIRST_PERSON;
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options != null) {
                var cameraTypeEnum = mc.options.getCameraType();
                if (cameraTypeEnum.isFirstPerson()) {
                    cameraType = CameraDataHeader.CAMERA_FIRST_PERSON;
                } else if (cameraTypeEnum.name().contains("BACK")) {
                    cameraType = CameraDataHeader.CAMERA_THIRD_BACK;
                } else if (cameraTypeEnum.name().contains("FRONT")) {
                    cameraType = CameraDataHeader.CAMERA_THIRD_FRONT;
                }
            }

            boolean detached = camera.isDetached();

            // Write to shared memory
            sharedMemory.writeCameraData(position, xRot, yRot, rotation, fov, cameraType, detached);

        } catch (Exception e) {
            LOGGER.error("Failed to write camera data", e);
        }
    }

    /**
     * Enable or disable camera data capture.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LOGGER.info("Camera data capture {}", enabled ? "enabled" : "disabled");
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the write interval.
     */
    public void setWriteInterval(long intervalNs) {
        this.writeIntervalNs = intervalNs;
    }

    public long getWriteInterval() {
        return writeIntervalNs;
    }

    /**
     * Set target FPS.
     */
    public void setTargetFps(int fps) {
        if (fps < 1) fps = 1;
        if (fps > 60) fps = 60;
        this.writeIntervalNs = 1_000_000_000L / (long) fps;
    }

    public int getTargetFps() {
        return (int) (1_000_000_000L / writeIntervalNs);
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Shutdown and cleanup resources.
     */
    public void shutdown() {
        LOGGER.info("Shutting down CameraDataWriter");
        if (sharedMemory != null) {
            try {
                sharedMemory.close();
            } catch (Exception e) {
                LOGGER.error("Error closing camera shared memory", e);
            }
            sharedMemory = null;
        }
        initialized = false;
    }
}

package com.mshare.screen;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Core screen capture logic using Minecraft's native GPU API.
 * 
 * This implementation mirrors Minecraft's Screenshot.java approach:
 * - Uses RenderSystem.getDevice() to create GPU buffers
 * - Uses CommandEncoder.copyTextureToBuffer() to copy texture to CPU-readable buffer
 * - Uses mapBuffer() to read pixel data after GPU operation completes
 * 
 * Layout (64 bytes header):
 *   0  magic:       4 bytes  - "MCSH" (0x4D435348)
 *   4  version:     4 bytes  - 1
 *   8  width:       4 bytes  - screen width
 *  12  height:      4 bytes  - screen height
 *  16  format:      4 bytes  - 1 (RGBA)
 *  20  stride:      4 bytes  - bytes per row (width*4)
 *  24  timestamp:   8 bytes  - nanoseconds
 *  32  frameCount:  8 bytes  - frame counter (long)
 *  40  status:      4 bytes  - 0=idle, 1=writing, 2=ready
 *  44  reserved:    20 bytes
 *  64  pixel data
 */
public final class ScreenCapture {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenCapture.class);

    private static final int HEADER_SIZE = 64;
    private static final int STATUS_IDLE = 0;
    private static final int STATUS_WRITING = 1;
    private static final int STATUS_READY = 2;

    private NamedSharedMemory sharedMemory;
    private ByteBuffer sharedBuffer;

    private int frameCount = 0;
    private long lastCaptureTime = 0;
    private long captureIntervalNs = 33_333_333L; // ~30 FPS
    private int downscaleFactor = 1;
    private boolean initialized = false;

    // Track current capture dimensions
    private int currentWidth = 0;
    private int currentHeight = 0;

    // Cached GPU buffer for reading (recreated on resize)
    private GpuBuffer gpuBuffer;
    private int gpuBufferSize;

    private ScreenCapture() {}

    public static ScreenCapture create() {
        return new ScreenCapture();
    }

    public void setTargetFps(int fps) {
        if (fps < 1) fps = 1;
        if (fps > 60) fps = 60;
        this.captureIntervalNs = 1_000_000_000L / (long) fps;
    }

    public void setDownscaleFactor(int factor) {
        if (factor < 1) factor = 1;
        if (factor > 8) factor = 8;
        this.downscaleFactor = factor;
    }

    /**
     * Initialize shared memory.
     */
    public boolean init() {
        if (initialized) {
            return true;
        }

        try {
            RenderTarget mainRT = ScreenCaptureManager.getMainRenderTarget();
            if (mainRT == null) {
                LOGGER.error("mainRenderTarget is null");
                return false;
            }

            int rtWidth = mainRT.width;
            int rtHeight = mainRT.height;

            if (rtWidth <= 0 || rtHeight <= 0) {
                LOGGER.error("Invalid render target dimensions: {}x{}", rtWidth, rtHeight);
                return false;
            }

            currentWidth = rtWidth / downscaleFactor;
            currentHeight = rtHeight / downscaleFactor;

            long size = (long) currentWidth * currentHeight * 4L + HEADER_SIZE + 4096;
            sharedMemory = NamedSharedMemory.createServer(size);
            sharedBuffer = sharedMemory.getPointer().getByteBuffer(0, size);

            // Create GPU buffer for texture-to-buffer copy
            createGpuBuffer(rtWidth, rtHeight);

            initialized = true;
            LOGGER.info("Screen capture initialized (RT: {}x{}, capture: {}x{})",
                rtWidth, rtHeight, currentWidth, currentHeight);
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to initialize screen capture", e);
            return false;
        }
    }

    /**
     * Create GPU buffer for reading texture data.
     * Uses the same approach as Minecraft's Screenshot.java.
     */
    private void createGpuBuffer(int width, int height) {
        // Close existing buffer if any
        if (gpuBuffer != null) {
            try {
                gpuBuffer.close();
            } catch (Exception e) {
                LOGGER.warn("Error closing old GPU buffer: {}", e.getMessage());
            }
            gpuBuffer = null;
        }

        // Buffer needs: width * height * 4 bytes (RGBA)
        // Using usage flags: MAP_READ (1) + COPY_DST (8)
        gpuBufferSize = width * height * 4;
        gpuBuffer = RenderSystem.getDevice().createBuffer(
            () -> "ScreenCapture buffer",
            1 | 8,  // USAGE_MAP_READ | USAGE_COPY_DST
            gpuBufferSize
        );
        LOGGER.debug("Created GPU buffer of size {} bytes for {}x{}", gpuBufferSize, width, height);
    }

    /**
     * Capture a frame if enough time has passed.
     */
    public boolean captureIfNeeded() {
        long now = System.nanoTime();
        if (now - lastCaptureTime < captureIntervalNs) {
            return false;
        }
        return captureFrame();
    }

    /**
     * Force capture a frame immediately.
     * Uses Minecraft's native GPU API approach from Screenshot.java.
     */
    public boolean captureFrame() {
        RenderSystem.assertOnRenderThread();

        try {
            RenderTarget mainRT = ScreenCaptureManager.getMainRenderTarget();
            if (mainRT == null) {
                return false;
            }

            GpuTexture colorTexture = mainRT.getColorTexture();
            if (colorTexture == null) {
                LOGGER.warn("Color texture is null");
                return false;
            }

            int rtWidth = mainRT.width;
            int rtHeight = mainRT.height;

            if (rtWidth <= 0 || rtHeight <= 0) {
                return false;
            }

            int captureWidth = rtWidth / downscaleFactor;
            int captureHeight = rtHeight / downscaleFactor;

            // Re-init if dimensions changed
            if (currentWidth != captureWidth || currentHeight != captureHeight) {
                currentWidth = captureWidth;
                currentHeight = captureHeight;

                if (sharedMemory != null) {
                    try {
                        sharedMemory.close();
                    } catch (Exception ignored) {}
                }
                long size = (long) captureWidth * captureHeight * 4L + HEADER_SIZE + 4096;
                sharedMemory = NamedSharedMemory.createServer(size);
                sharedBuffer = sharedMemory.getPointer().getByteBuffer(0, size);

                // Recreate GPU buffer for new size
                createGpuBuffer(rtWidth, rtHeight);

                LOGGER.info("Screen capture resized to {}x{}", captureWidth, captureHeight);
            }

            if (sharedBuffer == null) {
                return false;
            }

            if (gpuBuffer == null) {
                LOGGER.warn("GPU buffer is null, recreating...");
                createGpuBuffer(rtWidth, rtHeight);
                if (gpuBuffer == null) {
                    return false;
                }
            }

            // Mark as writing
            sharedBuffer.putInt(40, STATUS_WRITING);

            // Write header (will be updated with final values after GPU read)
            sharedBuffer.putInt(0, 0x4D435348); // "MCSH"
            sharedBuffer.putInt(4, 1); // version
            sharedBuffer.putInt(8, captureWidth);
            sharedBuffer.putInt(12, captureHeight);
            sharedBuffer.putInt(16, 1); // RGBA format
            sharedBuffer.putInt(20, captureWidth * 4); // stride

            // Use Minecraft's approach: copyTextureToBuffer with callback
            // This ensures GPU operation completes before reading
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

            encoder.copyTextureToBuffer(colorTexture, gpuBuffer, 0L, () -> {
                // This callback runs after GPU copy completes
                try (GpuBuffer.MappedView read = encoder.mapBuffer(gpuBuffer, true, false)) {
                    ByteBuffer data = read.data();
                    int pixelStride = colorTexture.getFormat().pixelSize(); // 4 for RGBA8

                    // Process pixels from GPU buffer to shared memory
                    // Following MC's Screenshot.java logic:
                    // - Input from GPU is RGBA bytes
                    // - getInt() reads as big-endian: [R,G,B,A] -> ABGR integer
                    // - Y needs to be flipped (GPU origin is bottom-left)
                    // - Output to shared memory should be ARGB for BufferedImage compatibility
                    
                    for (int y = 0; y < captureHeight; ++y) {
                        for (int x = 0; x < captureWidth; ++x) {
                            int argb;
                            
                            if (downscaleFactor == 1) {
                                // No downscaling - copy raw RGBA bytes directly
                                int offset = (x + y * rtWidth) * pixelStride;
                                byte r = data.get(offset);
                                byte g = data.get(offset + 1);
                                byte b = data.get(offset + 2);
                                byte a = data.get(offset + 3);
                                
                                // Write as ARGB to shared memory
                                argb = ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
                            } else {
                                // Downscaling - average NxN pixels
                                int red = 0, green = 0, blue = 0;
                                for (int i = 0; i < downscaleFactor; ++i) {
                                    for (int j = 0; j < downscaleFactor; ++j) {
                                        int readX = x * downscaleFactor + i;
                                        int readY = y * downscaleFactor + j;
                                        int byteOffset = (readX + readY * rtWidth) * pixelStride;
                                        
                                        red += data.get(byteOffset) & 0xFF;
                                        green += data.get(byteOffset + 1) & 0xFF;
                                        blue += data.get(byteOffset + 2) & 0xFF;
                                    }
                                }
                                int sampleCount = downscaleFactor * downscaleFactor;
                                argb = (0xFF << 24) | ((red / sampleCount) << 16) | 
                                       ((green / sampleCount) << 8) | (blue / sampleCount);
                            }

                            // Write to shared memory
                            int offset = HEADER_SIZE + (y * captureWidth + x) * 4;
                            sharedBuffer.putInt(offset, argb);
                        }
                    }

                    // Update header with final values
                    sharedBuffer.putLong(24, System.nanoTime());
                    sharedBuffer.putLong(32, ++frameCount);
                    sharedBuffer.putInt(40, STATUS_READY);

                    lastCaptureTime = System.nanoTime();

                } catch (Exception e) {
                    LOGGER.error("Error reading GPU buffer: {}", e.getMessage(), e);
                    if (sharedBuffer != null) {
                        sharedBuffer.putInt(40, STATUS_IDLE);
                    }
                }
            }, 0);

            return true;

        } catch (Exception e) {
            LOGGER.error("Frame capture failed: {}", e.getMessage(), e);
            if (sharedBuffer != null) {
                sharedBuffer.putInt(40, STATUS_IDLE);
            }
            return false;
        }
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getDownscaleFactor() {
        return downscaleFactor;
    }

    public int getTargetFps() {
        return (int) (1_000_000_000L / captureIntervalNs);
    }

    public void shutdown() {
        // Only close GPU buffer if OpenGL context is available
        // During Minecraft shutdown, the context may already be destroyed
        if (gpuBuffer != null) {
            try {
                // Check if we can safely close - if not on render thread during shutdown,
                // the context may be invalid
                if (RenderSystem.isOnRenderThread()) {
                    gpuBuffer.close();
                } else {
                    // Context may be invalid, just null it out
                    LOGGER.warn("OpenGL context may be invalid during shutdown, skipping GPU buffer close");
                }
            } catch (Exception e) {
                LOGGER.warn("Error closing GPU buffer: {}", e.getMessage());
            }
            gpuBuffer = null;
        }
        if (sharedMemory != null) {
            try {
                sharedMemory.close();
            } catch (Exception e) {
                LOGGER.error("Error closing shared memory", e);
            }
            sharedMemory = null;
            sharedBuffer = null;
        }
        initialized = false;
        LOGGER.info("ScreenCapture shutdown");
    }
}

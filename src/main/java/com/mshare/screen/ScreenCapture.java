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
 *   4  version:     4 bytes  - 2
 *   8  screenWidth: 4 bytes  - actual window framebuffer width
 *  12  screenHeight:4 bytes  - actual window framebuffer height
 *  16  width:       4 bytes  - capture width (after downscale)
 *  20  height:      4 bytes  - capture height (after downscale)
 *  24  format:      4 bytes  - 1 (RGBA)
 *  28  stride:      4 bytes  - bytes per row (width*4)
 *  32  reserved:    4 bytes  - reserved
 *  36  timestamp:   8 bytes  - nanoseconds
 *  44  frameCount:  8 bytes  - frame counter (long)
 *  52  status:      4 bytes  - 0=idle, 1=writing, 2=ready
 *  56  reserved:    8 bytes
 *  64  pixel data
 *
 * Resize safety:
 *   Shared memory is allocated at a FIXED maximum size (MAX_WIDTH * MAX_HEIGHT * 4 + HEADER_SIZE)
 *   at init time and is NEVER re-created during window resize. The header fields (width, height,
 *   screenWidth, screenHeight, stride) are updated on every frame so clients can detect resize
 *   events and reconnect if needed. The GPU buffer is the only resource that is re-created on resize.
 */
public final class ScreenCapture {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenCapture.class);

    // Fixed max capture dimensions - used to size the shared memory buffer.
    // A shared memory buffer of this size is allocated once at init and is NEVER
    // re-created during window resize. This prevents crashes from dangling pointers.
    private static final int MAX_WIDTH = 3840;
    private static final int MAX_HEIGHT = 2160;
    private static final int MAX_PIXEL_SIZE = MAX_WIDTH * MAX_HEIGHT * 4;
    private static final int HEADER_SIZE = 64;

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_WRITING = 1;
    private static final int STATUS_READY = 2;

    // Version 2 header offsets
    private static final int OFF_MAGIC = 0;
    private static final int OFF_VERSION = 4;
    private static final int OFF_SCREEN_WIDTH = 8;
    private static final int OFF_SCREEN_HEIGHT = 12;
    private static final int OFF_WIDTH = 16;
    private static final int OFF_HEIGHT = 20;
    private static final int OFF_FORMAT = 24;
    private static final int OFF_STRIDE = 28;
    private static final int OFF_TIMESTAMP = 36;
    private static final int OFF_FRAME_COUNT = 44;
    private static final int OFF_STATUS = 52;

    private NamedSharedMemory sharedMemory;
    private ByteBuffer sharedBuffer;

    private int frameCount = 0;
    private long lastCaptureTime = 0;
    private long captureIntervalNs = 33_333_333L; // ~30 FPS
    private int downscaleFactor = 1;
    private boolean initialized = false;

    // Current dimensions as last captured (for resize detection)
    private int currentScreenWidth = 0;
    private int currentScreenHeight = 0;
    private int currentCaptureWidth = 0;
    private int currentCaptureHeight = 0;

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
     * Allocates a FIXED-SIZE buffer (MAX_WIDTH * MAX_HEIGHT * 4 + HEADER_SIZE) once.
     * This buffer is never re-created during window resize.
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

            currentScreenWidth = rtWidth;
            currentScreenHeight = rtHeight;
            currentCaptureWidth = rtWidth / downscaleFactor;
            currentCaptureHeight = rtHeight / downscaleFactor;

            // Allocate a FIXED-SIZE shared memory buffer large enough for max resolution.
            // This is done ONCE at init and is NEVER re-created on window resize.
            // The client can detect resize by comparing screenWidth/screenHeight in the header.
            long size = (long) MAX_PIXEL_SIZE + HEADER_SIZE;
            sharedMemory = NamedSharedMemory.createServer(size);
            sharedBuffer = sharedMemory.getPointer().getByteBuffer(0, size);

            // Write initial header
            writeHeader(rtWidth, rtHeight, currentCaptureWidth, currentCaptureHeight);
            resetHeaderStatus();

            // Create GPU buffer for texture-to-buffer copy
            createGpuBuffer(rtWidth, rtHeight);

            initialized = true;
            LOGGER.info("Screen capture initialized (screen: {}x{}, capture: {}x{}, max buffer: {}x{})",
                rtWidth, rtHeight, currentCaptureWidth, currentCaptureHeight, MAX_WIDTH, MAX_HEIGHT);
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to initialize screen capture", e);
            return false;
        }
    }

    /**
     * Write the header to shared memory.
     * Only writes fields that change on resize. Does NOT modify status.
     * The status field is managed separately per-frame by captureFrame().
     */
    private void writeHeader(int screenWidth, int screenHeight, int captureWidth, int captureHeight) {
        sharedBuffer.putInt(OFF_MAGIC, 0x4D435348); // "MCSH"
        sharedBuffer.putInt(OFF_VERSION, 2);
        sharedBuffer.putInt(OFF_SCREEN_WIDTH, screenWidth);
        sharedBuffer.putInt(OFF_SCREEN_HEIGHT, screenHeight);
        sharedBuffer.putInt(OFF_WIDTH, captureWidth);
        sharedBuffer.putInt(OFF_HEIGHT, captureHeight);
        sharedBuffer.putInt(OFF_FORMAT, 1); // RGBA
        sharedBuffer.putInt(OFF_STRIDE, captureWidth * 4);
        // Note: do NOT write status here — it's managed per-frame by captureFrame()
    }

    /**
     * Reset header status to IDLE.
     * Called at init and resize time (not during capture).
     */
    private void resetHeaderStatus() {
        sharedBuffer.putInt(OFF_STATUS, STATUS_IDLE);
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

            // Handle resize: update header and recreate GPU buffer if dimensions changed.
            // The shared memory buffer itself is NEVER closed/re-created — it has a fixed
            // max-size allocation that handles any window size up to MAX_WIDTH x MAX_HEIGHT.
            boolean screenResized = rtWidth != currentScreenWidth || rtHeight != currentScreenHeight;
            boolean captureResized = captureWidth != currentCaptureWidth || captureHeight != currentCaptureHeight;

            if (screenResized || captureResized) {
                currentScreenWidth = rtWidth;
                currentScreenHeight = rtHeight;
                currentCaptureWidth = captureWidth;
                currentCaptureHeight = captureHeight;

                // Update the header with new dimensions
                writeHeader(rtWidth, rtHeight, captureWidth, captureHeight);

                // Recreate GPU buffer for the new screen size
                createGpuBuffer(rtWidth, rtHeight);

                LOGGER.info("Screen capture resized: screen={}x{}, capture={}x{}",
                    rtWidth, rtHeight, captureWidth, captureHeight);
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
            sharedBuffer.putInt(OFF_STATUS, STATUS_WRITING);

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
                    sharedBuffer.putLong(OFF_TIMESTAMP, System.nanoTime());
                    sharedBuffer.putLong(OFF_FRAME_COUNT, ++frameCount);
                    sharedBuffer.putInt(OFF_STATUS, STATUS_READY);

                    lastCaptureTime = System.nanoTime();

                } catch (Exception e) {
                    LOGGER.error("Error reading GPU buffer: {}", e.getMessage(), e);
                    if (sharedBuffer != null) {
                        sharedBuffer.putInt(OFF_STATUS, STATUS_IDLE);
                    }
                }
            }, 0);

            return true;

        } catch (Exception e) {
            LOGGER.error("Frame capture failed: {}", e.getMessage(), e);
            if (sharedBuffer != null) {
                sharedBuffer.putInt(OFF_STATUS, STATUS_IDLE);
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

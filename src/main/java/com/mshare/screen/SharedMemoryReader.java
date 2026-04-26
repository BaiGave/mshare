package com.mshare.screen;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Standalone shared memory reader - no external dependencies.
 * Uses Windows API via JNA to read shared memory.
 * Continuously reads frames in a loop.
 *
 * Header layout (v2, 64 bytes total):
 *   0  magic:        4 bytes - "MCSH" (0x4D435348)
 *   4  version:       4 bytes - 2
 *   8  screenWidth:   4 bytes - actual window framebuffer width
 *  12  screenHeight:  4 bytes - actual window framebuffer height
 *  16  width:         4 bytes - capture width (after downscale)
 *  20  height:        4 bytes - capture height (after downscale)
 *  24  format:        4 bytes - 1 (RGBA)
 *  28  stride:        4 bytes - bytes per row
 *  32  reserved:      4 bytes
 *  36  timestamp:      8 bytes - nanoseconds
 *  44  frameCount:     8 bytes
 *  52  status:         4 bytes - 0=idle, 1=writing, 2=ready
 *  56  reserved:       8 bytes
 *  64  pixel data
 *
 * Resize support:
 *   The writer allocates a fixed max-size buffer at init time and never closes
 *   the shared memory on resize. This reader detects resize events by monitoring
 *   screenWidth/screenHeight in the header and skips frames during transition.
 */
public class SharedMemoryReader {
    private static final int FILE_MAP_ALL_ACCESS = 0xF001F;
    private static final String MAPPING_NAME = "Global\\MinecraftScreenCapture";
    private static final int HEADER_SIZE = 64;
    private static final int STATUS_READY = 2;
    private static final int MAGIC = 0x4D435348;
    private static final int VERSION = 2;

    private static final Kernel32 kernel32 = Native.load("kernel32", Kernel32.class);

    // Header field offsets
    private static final int OFF_MAGIC = 0;
    private static final int OFF_VERSION = 4;
    private static final int OFF_SCREEN_WIDTH = 8;
    private static final int OFF_SCREEN_HEIGHT = 12;
    private static final int OFF_WIDTH = 16;
    private static final int OFF_HEIGHT = 20;
    private static final int OFF_STRIDE = 28;
    private static final int OFF_TIMESTAMP = 36;
    private static final int OFF_FRAME_COUNT = 44;
    private static final int OFF_STATUS = 52;

    public interface Kernel32 extends Library {
        Pointer OpenFileMappingW(int dwDesiredAccess, boolean bInheritHandle, String lpName);
        Pointer MapViewOfFile(Pointer hFileMappingObject, int dwDesiredAccess,
                             int dwFileOffsetHigh, int dwFileOffsetLow, long dwNumberOfBytesToMap);
        boolean UnmapViewOfFile(Pointer lpBaseAddress);
        boolean CloseHandle(Pointer hObject);
    }

    public static void main(String[] args) {
        System.out.println("[Reader] Starting shared memory reader...");

        int frameCount = 0;
        long lastFrameCount = -1;
        int errorCount = 0;

        // Track dimensions for resize detection
        int lastScreenWidth = -1;
        int lastScreenHeight = -1;
        int lastWidth = -1;
        int lastHeight = -1;

        while (errorCount < 10) {
            try {
                // Open shared memory
                Pointer fileMapping = kernel32.OpenFileMappingW(FILE_MAP_ALL_ACCESS, false, MAPPING_NAME);
                if (fileMapping == null || fileMapping == Pointer.NULL) {
                    // System.out.println("[Reader] Waiting for shared memory...");
                    Thread.sleep(100);
                    continue;
                }

                // Map the view - map the full max-size buffer
                Pointer mappedView = kernel32.MapViewOfFile(fileMapping, FILE_MAP_ALL_ACCESS, 0, 0, 0);
                if (mappedView == null || mappedView == Pointer.NULL) {
                    kernel32.CloseHandle(fileMapping);
                    Thread.sleep(100);
                    continue;
                }

                try {
                    while (true) {
                        // Read header
                        int magic = mappedView.getInt(OFF_MAGIC);
                        int version = mappedView.getInt(OFF_VERSION);
                        int screenWidth = mappedView.getInt(OFF_SCREEN_WIDTH);
                        int screenHeight = mappedView.getInt(OFF_SCREEN_HEIGHT);
                        int width = mappedView.getInt(OFF_WIDTH);
                        int height = mappedView.getInt(OFF_HEIGHT);
                        int stride = mappedView.getInt(OFF_STRIDE);
                        int status = mappedView.getInt(OFF_STATUS);
                        long currentFrameCount = mappedView.getLong(OFF_FRAME_COUNT);

                        if (magic != MAGIC) {
                            System.out.println("[Reader] Invalid magic: " + String.format("0x%08X", magic));
                            break;
                        }

                        if (version != VERSION) {
                            System.out.println("[Reader] Unsupported version: " + version + " (expected " + VERSION + ")");
                            break;
                        }

                        // Detect resize - if dimensions changed significantly, reset tracking
                        boolean resized = (screenWidth != lastScreenWidth || screenHeight != lastScreenHeight
                                || width != lastWidth || height != lastHeight);
                        if (resized) {
                            System.out.println("[Reader] Resize detected: screen=" + screenWidth + "x" + screenHeight
                                    + ", capture=" + width + "x" + height);
                            lastScreenWidth = screenWidth;
                            lastScreenHeight = screenHeight;
                            lastWidth = width;
                            lastHeight = height;
                            lastFrameCount = -1; // Reset frame tracking after resize
                        }

                        // Check if there's a new frame
                        if (status == STATUS_READY && currentFrameCount != lastFrameCount) {
                            if (width > 0 && height > 0 && width <= 3840 && height <= 2160) {
                                lastFrameCount = currentFrameCount;

                                // Stride must equal width*4 (RGBA, no padding with GL_PACK_ALIGNMENT=1)
                                int expectedStride = width * 4;
                                if (stride != expectedStride) {
                                    System.out.println("[Reader] Stride mismatch: header=" + stride + " expected=" + expectedStride + ", skipping frame");
                                    continue;
                                }
                                int pixelDataSize = height * stride;

                                // Read pixel data from shared memory
                                byte[] pixelData = new byte[pixelDataSize];
                                mappedView.read(HEADER_SIZE, pixelData, 0, pixelDataSize);

                                // Convert to BufferedImage
                                // OpenGL reads pixels bottom-to-top, need to flip Y axis
                                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                                int[] pixels = new int[width * height];
                                for (int y = 0; y < height; y++) {
                                    // Flip Y axis - OpenGL has origin at bottom-left
                                    int flippedY = height - 1 - y;
                                    for (int x = 0; x < width; x++) {
                                        int srcIdx = flippedY * stride + x * 4;
                                        // Memory order: BGRA
                                        int b = pixelData[srcIdx] & 0xFF;
                                        int g = pixelData[srcIdx + 1] & 0xFF;
                                        int r = pixelData[srcIdx + 2] & 0xFF;
                                        int a = pixelData[srcIdx + 3] & 0xFF;
                                        pixels[y * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
                                    }
                                }
                                image.setRGB(0, 0, width, height, pixels, 0, width);

                                // Encode to PNG
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageIO.write(image, "PNG", baos);
                                byte[] pngData = baos.toByteArray();

                                // Output frame
                                frameCount++;
                                // Use standard encoder (no line breaks), then manually split for transmission
                                String base64Data = Base64.getEncoder().encodeToString(pngData);
                                System.out.println("[Reader] FRAME_START");
                                System.out.println("[Reader] WIDTH:" + width);
                                System.out.println("[Reader] HEIGHT:" + height);
                                System.out.println("[Reader] SIZE:" + pngData.length);
                                System.out.println("[Reader] BASE64_LENGTH:" + base64Data.length());
                                // Split Base64 into multiple lines for reliable transmission
                                // Each line prefixed with [Reader] DATA: and max 76 chars
                                int lineLength = 76;
                                int chunkCount = 0;
                                for (int i = 0; i < base64Data.length(); i += lineLength) {
                                    int end = Math.min(i + lineLength, base64Data.length());
                                    System.out.println("[Reader] DATA:" + base64Data.substring(i, end));
                                    chunkCount++;
                                }
                                System.out.println("[Reader] CHUNKS:" + chunkCount);
                                System.out.println("[Reader] FRAME_END");
                                System.out.flush();

                                errorCount = 0; // Reset error count on successful frame
                            }
                        }

                        // Small delay to avoid busy loop
                        Thread.sleep(16); // ~60 FPS polling
                    }
                } finally {
                    kernel32.UnmapViewOfFile(mappedView);
                    kernel32.CloseHandle(fileMapping);
                }
            } catch (Exception e) {
                errorCount++;
                System.out.println("[Reader] Error: " + e.getMessage());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }

        System.out.println("[Reader] Exiting after " + errorCount + " errors");
    }
}

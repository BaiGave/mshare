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
 */
public class SharedMemoryReader {
    private static final int FILE_MAP_ALL_ACCESS = 0xF001F;
    private static final String MAPPING_NAME = "Global\\MinecraftScreenCapture";
    private static final int HEADER_SIZE = 64;
    private static final int STATUS_READY = 2;

    private static final Kernel32 kernel32 = Native.load("kernel32", Kernel32.class);

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
        
        while (errorCount < 10) {
            try {
                // Open shared memory
                Pointer fileMapping = kernel32.OpenFileMappingW(FILE_MAP_ALL_ACCESS, false, MAPPING_NAME);
                if (fileMapping == null || fileMapping == Pointer.NULL) {
                    // System.out.println("[Reader] Waiting for shared memory...");
                    Thread.sleep(100);
                    continue;
                }

                // Map the view
                Pointer mappedView = kernel32.MapViewOfFile(fileMapping, FILE_MAP_ALL_ACCESS, 0, 0, 0);
                if (mappedView == null || mappedView == Pointer.NULL) {
                    kernel32.CloseHandle(fileMapping);
                    Thread.sleep(100);
                    continue;
                }

                try {
                    while (true) {
                        // Read header
                        // Layout: magic(0), width(8), height(12), format(16), stride(20),
                        //         timestamp(24,8bytes), frameCount(32,8bytes), status(40)
                        int magic = mappedView.getInt(0);
                        int width = mappedView.getInt(8);
                        int height = mappedView.getInt(12);
                        int status = mappedView.getInt(40);
                        long currentFrameCount = mappedView.getLong(32);

                        if (magic != 0x4D435348) {
                            System.out.println("[Reader] Invalid magic: " + String.format("0x%08X", magic));
                            break;
                        }

                        // Check if there's a new frame
                        if (status == STATUS_READY && currentFrameCount != lastFrameCount) {
                            if (width > 0 && height > 0 && width <= 3840 && height <= 2160) {
                                lastFrameCount = currentFrameCount;

                                // Compute actual stride from shared memory header
                                // Stride must equal width*4 (RGBA, no padding with GL_PACK_ALIGNMENT=1)
                                int actualStride = mappedView.getInt(20);
                                int expectedStride = width * 4;
                                if (actualStride != expectedStride) {
                                    System.out.println("[Reader] Stride mismatch: header=" + actualStride + " expected=" + expectedStride + ", skipping frame");
                                    continue;
                                }
                                int pixelDataSize = height * actualStride;

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
                                        int srcIdx = flippedY * actualStride + x * 4;
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

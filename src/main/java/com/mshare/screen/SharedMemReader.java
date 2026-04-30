package com.mshare.screen;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Shared Memory Reader using JNA directly.
 * Reads pixel data from shared memory for external consumption.
 */
public class SharedMemReader {
    private static final int FILE_MAP_READ = 0x0004;
    private static final int HEADER_SIZE = 64;

    // Offsets in header
    private static final int OFF_WIDTH = 16;
    private static final int OFF_HEIGHT = 20;
    private static final int OFF_STRIDE = 28;
    private static final int OFF_FRAME_COUNT = 44;
    private static final int OFF_STATUS = 52;
    private static final int STATUS_READY = 2;

    private HANDLE hFileMapping;
    private Pointer pData;
    private long mappedSize;

    public boolean open(String name) {
        try {
            String fullName = "Global\\" + name;
            System.err.println("[SharedMemReader] Opening: " + fullName);

            // Load JNA
            Native.getLastError(); // Ensure JNA is initialized

            // Open file mapping
            hFileMapping = Kernel32.INSTANCE.OpenFileMapping(FILE_MAP_READ, false, fullName);
            if (hFileMapping == null) {
                int err = Native.getLastError();
                System.err.println("[SharedMemReader] OpenFileMappingW failed, error: " + err);
                return false;
            }

            // Map view (cast to int for JNA)
            mappedSize = 3840L * 2160L * 4 + HEADER_SIZE;
            pData = Kernel32.INSTANCE.MapViewOfFile(hFileMapping, FILE_MAP_READ, 0, 0, (int) mappedSize);
            if (pData == null) {
                int err = Native.getLastError();
                System.err.println("[SharedMemReader] MapViewOfFile failed, error: " + err);
                Kernel32.INSTANCE.CloseHandle(hFileMapping);
                hFileMapping = null;
                return false;
            }

            System.err.println("[SharedMemReader] Connected successfully");
            return true;

        } catch (Exception e) {
            System.err.println("[SharedMemReader] Open failed: " + e);
            return false;
        }
    }

    public void close() {
        if (pData != null) {
            try {
                Kernel32.INSTANCE.UnmapViewOfFile(pData);
            } catch (Exception e) {
                System.err.println("[SharedMemReader] UnmapViewOfFile error: " + e);
            }
            pData = null;
        }
        if (hFileMapping != null) {
            Kernel32.INSTANCE.CloseHandle(hFileMapping);
            hFileMapping = null;
        }
        System.err.println("[SharedMemReader] Closed");
    }

    public int readInt(long offset) {
        if (pData == null) return 0;
        return pData.getInt(offset);
    }

    public long readLong(long offset) {
        if (pData == null) return 0;
        return pData.getLong(offset);
    }

    public int getWidth() {
        return readInt(OFF_WIDTH);
    }

    public int getHeight() {
        return readInt(OFF_HEIGHT);
    }

    public int getStatus() {
        return readInt(OFF_STATUS);
    }

    public long getFrameCount() {
        return readLong(OFF_FRAME_COUNT);
    }

    /**
     * Read pixel data starting from header offset.
     * Returns BGRA format pixels.
     */
    public byte[] readPixels(int width, int height) {
        if (pData == null) return null;

        int size = width * height * 4;
        byte[] pixels = new byte[size];

        // Read directly from native memory
        for (int i = 0; i < size; i++) {
            pixels[i] = pData.getByte(HEADER_SIZE + i);
        }

        return pixels;
    }

    public static void main(String[] args) throws Exception {
        System.err.println("[BinaryReader] Starting binary reader...");

        SharedMemReader reader = new SharedMemReader();
        if (!reader.open("MinecraftScreenCapture")) {
            System.err.println("[BinaryReader] Failed to open shared memory");
            return;
        }

        // Wait for Minecraft to create the shared memory
        int waitCount = 0;
        while (reader.getWidth() == 0 && waitCount < 100) {
            Thread.sleep(100);
            waitCount++;
        }

        if (reader.getWidth() == 0) {
            System.err.println("[BinaryReader] Timeout waiting for Minecraft");
            reader.close();
            return;
        }

        System.err.println("[BinaryReader] Connected, starting capture loop");

        long lastFrameCount = -1;

        while (true) {
            try {
                int status = reader.getStatus();
                long frameCount = reader.getFrameCount();
                int width = reader.getWidth();
                int height = reader.getHeight();

                if (status == STATUS_READY && frameCount > lastFrameCount && width > 0 && height > 0) {
                    lastFrameCount = frameCount;

                    byte[] pixels = reader.readPixels(width, height);
                    if (pixels != null) {
                        // Output binary frame
                        // Format: magic(4) + width(4) + height(4) + timestamp(8) + pixels(N)
                        ByteBuffer frame = ByteBuffer.allocate(20 + pixels.length);
                        frame.order(ByteOrder.LITTLE_ENDIAN);
                        frame.putInt(0x4D435348); // "MCSH"
                        frame.putInt(width);
                        frame.putInt(height);
                        frame.putLong(System.nanoTime());
                        frame.put(pixels);

                        // Write to stdout
                        System.out.flush();
                        System.out.write(frame.array());
                        System.out.flush();
                    }
                }

                Thread.sleep(16); // ~60 FPS
            } catch (Exception e) {
                System.err.println("[BinaryReader] Error: " + e);
                Thread.sleep(100);
            }
        }
    }
}

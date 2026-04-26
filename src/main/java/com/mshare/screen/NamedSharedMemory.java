package com.mshare.screen;

import com.sun.jna.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Windows Named Shared Memory implementation using JNA.
 * Creates shared memory accessible across Windows Sessions.
 */
public final class NamedSharedMemory implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NamedSharedMemory.class);

    private static final int FILE_MAP_ALL_ACCESS = 0xF001F;
    private static final int PAGE_READWRITE = 0x04;

    // 使用 Global\ 前缀实现跨 Windows Session 访问
    public static final String MAPPING_NAME = "Global\\MinecraftScreenCapture";
    public static final String MESH_MAPPING_NAME = "Global\\MCMeshData";

    private final Kernel32 kernel32;
    private Pointer fileMapping;
    private Pointer mappedView;
    private long mappedSize;
    private boolean isServer;

    private NamedSharedMemory() {
        this.kernel32 = Native.load("kernel32", Kernel32.class);
    }

    /**
     * Create shared memory with world-readable and world-writable security descriptor.
     */
    public static NamedSharedMemory createServer(long size) {
        NamedSharedMemory nsm = new NamedSharedMemory();
        nsm.isServer = true;

        // Create with NULL security descriptor (allows everyone to access)
        // On Windows, NULL DACL means everyone can access
        nsm.fileMapping = nsm.kernel32.CreateFileMappingW(
            new Pointer(-1), // INVALID_HANDLE_VALUE
            null, // NULL security (world-accessible)
            PAGE_READWRITE,
            (int) (size >>> 32),
            (int) size,
            MAPPING_NAME
        );

        if (nsm.fileMapping == null || nsm.fileMapping == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.error("Failed to create file mapping. Error code: {}", error);
            throw new RuntimeException("CreateFileMapping failed with error: " + error);
        }
        
        LOGGER.info("Created shared memory '{}' (size={})", MAPPING_NAME, size);

        // Map the view
        nsm.mappedView = nsm.kernel32.MapViewOfFile(
            nsm.fileMapping,
            FILE_MAP_ALL_ACCESS,
            0,
            0,
            size
        );

        if (nsm.mappedView == null || nsm.mappedView == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.error("Failed to map view. Error code: {}", error);
            nsm.kernel32.CloseHandle(nsm.fileMapping);
            throw new RuntimeException("MapViewOfFile failed: " + error);
        }

        nsm.mappedSize = size;
        LOGGER.info("Mapped shared memory view");
        return nsm;
    }

    /**
     * Connect as client.
     */
    public static NamedSharedMemory connectClient() {
        NamedSharedMemory nsm = new NamedSharedMemory();
        nsm.isServer = false;

        nsm.fileMapping = nsm.kernel32.OpenFileMappingW(FILE_MAP_ALL_ACCESS, false, MAPPING_NAME);

        if (nsm.fileMapping == null || nsm.fileMapping == Pointer.NULL) {
            LOGGER.warn("Shared memory '{}' does not exist", MAPPING_NAME);
            return null;
        }

        nsm.mappedView = nsm.kernel32.MapViewOfFile(
            nsm.fileMapping,
            FILE_MAP_ALL_ACCESS,
            0,
            0,
            0
        );

        if (nsm.mappedView == null || nsm.mappedView == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.warn("Failed to map view. Error: {}", error);
            nsm.kernel32.CloseHandle(nsm.fileMapping);
            return null;
        }

        nsm.mappedSize = 1920 * 1080 * 4 + 1024;
        LOGGER.info("Connected to shared memory '{}'", MAPPING_NAME);
        return nsm;
    }

    public Pointer getPointer() {
        if (mappedView == null || mappedView == Pointer.NULL) {
            throw new IllegalStateException("Memory not mapped");
        }
        return mappedView;
    }

    public long getMappedSize() {
        return mappedSize;
    }

    public boolean isServer() {
        return isServer;
    }

    @Override
    public void close() {
        if (mappedView != null && mappedView != Pointer.NULL) {
            kernel32.UnmapViewOfFile(mappedView);
            mappedView = Pointer.NULL;
        }
        if (fileMapping != null && fileMapping != Pointer.NULL) {
            kernel32.CloseHandle(fileMapping);
            fileMapping = Pointer.NULL;
        }
        LOGGER.info("Closed shared memory");
    }

    public interface Kernel32 extends Library {
        Pointer CreateFileMappingW(Pointer hFile, Structure.ByValue attrs,
                                  int flProtect, int highSize, int lowSize, String name);
        Pointer OpenFileMappingW(int dwDesiredAccess, boolean bInheritHandle, String lpName);
        Pointer MapViewOfFile(Pointer hFileMappingObject, int dwDesiredAccess,
                             int dwFileOffsetHigh, int dwFileOffsetLow, long dwNumberOfBytesToMap);
        boolean UnmapViewOfFile(Pointer lpBaseAddress);
        boolean CloseHandle(Pointer hObject);
    }
}

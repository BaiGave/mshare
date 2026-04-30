package com.mshare.screen;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for Windows Named Shared Memory operations.
 * Provides common functionality for creating/connecting to named shared memory.
 *
 * Subclasses must implement:
 * - getMappingName(): Return the unique name for this shared memory
 * - getSize(): Return the size of the shared memory
 * - initServer(): Perform server-specific initialization
 */
public abstract class BaseSharedMemory implements AutoCloseable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseSharedMemory.class);

    protected static final int FILE_MAP_ALL_ACCESS = 0xF001F;
    protected static final int PAGE_READWRITE = 0x04;

    protected final Kernel32 kernel32;
    protected Pointer fileMapping;
    protected Pointer mappedView;
    protected long mappedSize;
    protected boolean isServer;

    protected BaseSharedMemory() {
        this.kernel32 = Native.load("kernel32", Kernel32.class);
    }

    /**
     * Create shared memory as server (writer).
     */
    protected boolean createServer() {
        mappedSize = getSize();

        fileMapping = kernel32.CreateFileMappingW(
            new Pointer(-1),
            null,
            PAGE_READWRITE,
            (int) (mappedSize >>> 32),
            (int) mappedSize,
            getMappingName()
        );

        if (fileMapping == null || fileMapping == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.error("Failed to create file mapping '{}'. Error: {}", getMappingName(), error);
            return false;
        }

        LOGGER.info("Created shared memory '{}' (size={})", getMappingName(), mappedSize);

        mappedView = kernel32.MapViewOfFile(
            fileMapping,
            FILE_MAP_ALL_ACCESS,
            0,
            0,
            mappedSize
        );

        if (mappedView == null || mappedView == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.error("Failed to map view for '{}'. Error: {}", getMappingName(), error);
            kernel32.CloseHandle(fileMapping);
            fileMapping = null;
            return false;
        }

        isServer = true;
        LOGGER.info("Mapped shared memory view for '{}'", getMappingName());
        return true;
    }

    /**
     * Connect as client (reader).
     */
    protected boolean doConnect() {
        fileMapping = kernel32.OpenFileMappingW(FILE_MAP_ALL_ACCESS, false, getMappingName());

        if (fileMapping == null || fileMapping == Pointer.NULL) {
            LOGGER.warn("Shared memory '{}' does not exist", getMappingName());
            return false;
        }

        mappedView = kernel32.MapViewOfFile(
            fileMapping,
            FILE_MAP_ALL_ACCESS,
            0,
            0,
            0
        );

        if (mappedView == null || mappedView == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.warn("Failed to map view for '{}'. Error: {}", getMappingName(), error);
            kernel32.CloseHandle(fileMapping);
            fileMapping = null;
            return false;
        }

        mappedSize = getSize();
        isServer = false;
        LOGGER.info("Connected to shared memory '{}'", getMappingName());
        return true;
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
            try {
                kernel32.UnmapViewOfFile(mappedView);
            } catch (Exception e) {
                LOGGER.warn("Error unmapping view: {}", e.getMessage());
            }
            mappedView = Pointer.NULL;
        }
        if (fileMapping != null && fileMapping != Pointer.NULL) {
            try {
                kernel32.CloseHandle(fileMapping);
            } catch (Exception e) {
                LOGGER.warn("Error closing handle: {}", e.getMessage());
            }
            fileMapping = Pointer.NULL;
        }
        LOGGER.debug("Closed shared memory '{}'", getMappingName());
    }

    /**
     * Return the unique name for this shared memory mapping.
     */
    protected abstract String getMappingName();

    /**
     * Return the size of the shared memory buffer.
     */
    protected abstract long getSize();

    public interface Kernel32 extends Library {
        Pointer CreateFileMappingW(Pointer hFile, Structure attrs,
                                  int flProtect, int highSize, int lowSize, String name);
        Pointer OpenFileMappingW(int dwDesiredAccess, boolean bInheritHandle, String lpName);
        Pointer MapViewOfFile(Pointer hFileMappingObject, int dwDesiredAccess,
                             int dwFileOffsetHigh, int dwFileOffsetLow, long dwNumberOfBytesToMap);
        boolean UnmapViewOfFile(Pointer lpBaseAddress);
        boolean CloseHandle(Pointer hObject);
    }
}

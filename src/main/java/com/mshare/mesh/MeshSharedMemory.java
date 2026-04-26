package com.mshare.mesh;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.mshare.screen.MeshDataHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Windows Named Shared Memory for mesh data using JNA.
 * Creates shared memory accessible across Windows Sessions.
 */
public final class MeshSharedMemory implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeshSharedMemory.class);

    private static final int FILE_MAP_ALL_ACCESS = 0xF001F;
    private static final int PAGE_READWRITE = 0x04;

    // Use Global\ prefix for cross-session access
    private static final String MAPPING_NAME = "Global\\MCMeshData";

    // Default buffer size: 64MB (enough for a full chunk with all layers)
    private static final long DEFAULT_SIZE = 64 * 1024 * 1024;

    private final Kernel32 kernel32;
    private Pointer fileMapping;
    private Pointer mappedView;
    private long mappedSize;
    private boolean isServer;
    private long frameCount = 0;

    private MeshSharedMemory() {
        this.kernel32 = Native.load("kernel32", Kernel32.class);
    }

    /**
     * Create shared memory server with specified size.
     */
    public static MeshSharedMemory createServer(long size) {
        MeshSharedMemory nsm = new MeshSharedMemory();
        nsm.isServer = true;

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
            LOGGER.error("Failed to create mesh shared memory. Error code: {}", error);
            throw new RuntimeException("CreateFileMapping failed with error: " + error);
        }

        LOGGER.info("Created mesh shared memory '{}' (size={})", MAPPING_NAME, size);

        nsm.mappedView = nsm.kernel32.MapViewOfFile(
            nsm.fileMapping,
            FILE_MAP_ALL_ACCESS,
            0,
            0,
            size
        );

        if (nsm.mappedView == null || nsm.mappedView == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.error("Failed to map mesh view. Error code: {}", error);
            nsm.kernel32.CloseHandle(nsm.fileMapping);
            throw new RuntimeException("MapViewOfFile failed: " + error);
        }

        nsm.mappedSize = size;
        nsm.initHeader();
        LOGGER.info("Mapped mesh shared memory view");
        return nsm;
    }

    /**
     * Create server with default size.
     */
    public static MeshSharedMemory createServer() {
        return createServer(DEFAULT_SIZE);
    }

    /**
     * Connect as client.
     */
    public static MeshSharedMemory connectClient() {
        MeshSharedMemory nsm = new MeshSharedMemory();
        nsm.isServer = false;

        nsm.fileMapping = nsm.kernel32.OpenFileMappingW(FILE_MAP_ALL_ACCESS, false, MAPPING_NAME);

        if (nsm.fileMapping == null || nsm.fileMapping == Pointer.NULL) {
            LOGGER.warn("Mesh shared memory '{}' does not exist", MAPPING_NAME);
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
            LOGGER.warn("Failed to map mesh view. Error: {}", error);
            nsm.kernel32.CloseHandle(nsm.fileMapping);
            return null;
        }

        nsm.mappedSize = DEFAULT_SIZE;
        LOGGER.info("Connected to mesh shared memory '{}'", MAPPING_NAME);
        return nsm;
    }

    /**
     * Initialize header with default values.
     */
    private void initHeader() {
        if (mappedView == null || mappedView == Pointer.NULL) return;

        mappedView.setInt(OFFSET_MAGIC, MeshDataHeader.MAGIC);
        mappedView.setInt(OFFSET_VERSION, MeshDataHeader.VERSION);
        mappedView.setInt(OFFSET_STATUS, MeshDataHeader.STATUS_IDLE);
        mappedView.setLong(OFFSET_FRAME_COUNT, 0);

        // Initialize bounds to max values
        mappedView.setFloat(OFFSET_MIN_BOUND, Float.MAX_VALUE);
        mappedView.setFloat(OFFSET_MIN_BOUND + 4, Float.MAX_VALUE);
        mappedView.setFloat(OFFSET_MIN_BOUND + 8, Float.MAX_VALUE);
        mappedView.setFloat(OFFSET_MAX_BOUND, Float.MIN_VALUE);
        mappedView.setFloat(OFFSET_MAX_BOUND + 4, Float.MIN_VALUE);
        mappedView.setFloat(OFFSET_MAX_BOUND + 8, Float.MIN_VALUE);
    }

    /**
     * Write mesh data to shared memory.
     */
    public void writeMeshData(int vertexCount, int indexCount, int vertexSize,
                              int indexType, int flags,
                              float minX, float minY, float minZ,
                              float maxX, float maxY, float maxZ,
                              int renderTypeHash,
                              byte[] vertexData, byte[] indexData) {
        if (mappedView == null || mappedView == Pointer.NULL) {
            LOGGER.warn("Cannot write mesh data: memory not mapped");
            return;
        }

        // Wait if still writing
        while (mappedView.getInt(OFFSET_STATUS) == MeshDataHeader.STATUS_WRITING) {
            Thread.yield();
        }

        // Set status to writing
        mappedView.setInt(OFFSET_STATUS, MeshDataHeader.STATUS_WRITING);

        try {
            // Write header
            mappedView.setInt(OFFSET_VERTEX_COUNT, vertexCount);
            mappedView.setInt(OFFSET_INDEX_COUNT, indexCount);
            mappedView.setInt(OFFSET_VERTEX_SIZE, vertexSize);
            mappedView.setInt(OFFSET_INDEX_TYPE, indexType);
            mappedView.setInt(OFFSET_FLAGS, flags);
            mappedView.setInt(OFFSET_RENDER_TYPE_HASH, renderTypeHash);

            // Write bounds
            mappedView.setFloat(OFFSET_MIN_BOUND, minX);
            mappedView.setFloat(OFFSET_MIN_BOUND + 4, minY);
            mappedView.setFloat(OFFSET_MIN_BOUND + 8, minZ);
            mappedView.setFloat(OFFSET_MAX_BOUND, maxX);
            mappedView.setFloat(OFFSET_MAX_BOUND + 4, maxY);
            mappedView.setFloat(OFFSET_MAX_BOUND + 8, maxZ);

            // Write vertex data
            if (vertexData != null && vertexData.length > 0) {
                mappedView.write(MeshDataHeader.HEADER_SIZE, vertexData, 0, vertexData.length);
            }

            // Write index data
            if (indexData != null && indexData.length > 0) {
                int indexOffset = MeshDataHeader.HEADER_SIZE + vertexData.length;
                mappedView.write(indexOffset, indexData, 0, indexData.length);
            }

            // Increment frame count
            frameCount++;
            mappedView.setLong(OFFSET_FRAME_COUNT, frameCount);

            // Set status to ready
            mappedView.setInt(OFFSET_STATUS, MeshDataHeader.STATUS_READY);

            LOGGER.debug("Wrote mesh: {} vertices, {} indices, frame {}",
                vertexCount, indexCount, frameCount);

        } catch (Exception e) {
            LOGGER.error("Error writing mesh data: {}", e.getMessage());
            // Reset status on error
            mappedView.setInt(OFFSET_STATUS, MeshDataHeader.STATUS_IDLE);
        }
    }

    /**
     * Check if shared memory is valid.
     */
    public boolean isValid() {
        if (mappedView == null || mappedView == Pointer.NULL) return false;
        return mappedView.getInt(MeshDataHeader.OFFSET_MAGIC) == MeshDataHeader.MAGIC;
    }

    /**
     * Get current status.
     */
    public int getStatus() {
        if (mappedView == null || mappedView == Pointer.NULL) return -1;
        return mappedView.getInt(OFFSET_STATUS);
    }

    /**
     * Get current frame count.
     */
    public long getFrameCount() {
        if (mappedView == null || mappedView == Pointer.NULL) return -1;
        return mappedView.getLong(OFFSET_FRAME_COUNT);
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
        LOGGER.info("Closed mesh shared memory");
    }

    // Header offset constants (duplicate for convenience)
    private static final int OFFSET_MAGIC = 0;
    private static final int OFFSET_VERSION = 4;
    private static final int OFFSET_VERTEX_COUNT = 8;
    private static final int OFFSET_INDEX_COUNT = 12;
    private static final int OFFSET_VERTEX_SIZE = 16;
    private static final int OFFSET_INDEX_TYPE = 20;
    private static final int OFFSET_FLAGS = 24;
    private static final int OFFSET_MIN_BOUND = 28;
    private static final int OFFSET_MAX_BOUND = 40;
    private static final int OFFSET_RENDER_TYPE_HASH = 52;
    private static final int OFFSET_STATUS = 56;
    private static final int OFFSET_FRAME_COUNT = 60;

    public interface Kernel32 extends Library {
        Pointer CreateFileMappingW(Pointer hFile, Pointer attrs,
                                  int flProtect, int highSize, int lowSize, String name);
        Pointer OpenFileMappingW(int dwDesiredAccess, boolean bInheritHandle, String lpName);
        Pointer MapViewOfFile(Pointer hFileMappingObject, int dwDesiredAccess,
                             int dwFileOffsetHigh, int dwFileOffsetLow, long dwNumberOfBytesToMap);
        boolean UnmapViewOfFile(Pointer lpBaseAddress);
        boolean CloseHandle(Pointer hObject);
    }
}

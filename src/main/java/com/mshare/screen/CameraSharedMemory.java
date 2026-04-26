package com.mshare.screen;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Windows Named Shared Memory for camera data.
 * Creates shared memory accessible across Windows Sessions.
 */
public final class CameraSharedMemory implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraSharedMemory.class);

    private static final int FILE_MAP_ALL_ACCESS = 0xF001F;
    private static final int PAGE_READWRITE = 0x04;

    private static final String MAPPING_NAME = "Global\\MCCameraData";

    private final Kernel32 kernel32;
    private Pointer fileMapping;
    private Pointer mappedView;
    private long mappedSize;
    private boolean isServer;

    private CameraSharedMemory() {
        this.kernel32 = Native.load("kernel32", Kernel32.class);
    }

    /**
     * Create shared memory server for camera data.
     */
    public static CameraSharedMemory createServer(long size) {
        CameraSharedMemory csm = new CameraSharedMemory();
        csm.isServer = true;
        csm.mappedSize = size;

        // Create with NULL security descriptor (allows everyone to access)
        csm.fileMapping = csm.kernel32.CreateFileMappingW(
            new Pointer(-1), // INVALID_HANDLE_VALUE
            null, // NULL security (world-accessible)
            PAGE_READWRITE,
            (int) (size >>> 32),
            (int) size,
            MAPPING_NAME
        );

        if (csm.fileMapping == null || csm.fileMapping == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.error("Failed to create camera file mapping. Error code: {}", error);
            throw new RuntimeException("CreateFileMapping failed with error: " + error);
        }

        LOGGER.info("Created camera shared memory '{}' (size={})", MAPPING_NAME, size);

        // Map the view
        csm.mappedView = csm.kernel32.MapViewOfFile(
            csm.fileMapping,
            FILE_MAP_ALL_ACCESS,
            0,
            0,
            size
        );

        if (csm.mappedView == null || csm.mappedView == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.error("Failed to map camera view. Error code: {}", error);
            csm.kernel32.CloseHandle(csm.fileMapping);
            throw new RuntimeException("MapViewOfFile failed: " + error);
        }

        // Initialize header with magic and version
        csm.initHeader();

        LOGGER.info("Mapped camera shared memory view");
        return csm;
    }

    /**
     * Connect as client to read camera data.
     */
    public static CameraSharedMemory connectClient() {
        CameraSharedMemory csm = new CameraSharedMemory();
        csm.isServer = false;
        csm.mappedSize = CameraDataHeader.SIZE;

        csm.fileMapping = csm.kernel32.OpenFileMappingW(FILE_MAP_ALL_ACCESS, false, MAPPING_NAME);

        if (csm.fileMapping == null || csm.fileMapping == Pointer.NULL) {
            LOGGER.warn("Camera shared memory '{}' does not exist", MAPPING_NAME);
            return null;
        }

        csm.mappedView = csm.kernel32.MapViewOfFile(
            csm.fileMapping,
            FILE_MAP_ALL_ACCESS,
            0,
            0,
            0
        );

        if (csm.mappedView == null || csm.mappedView == Pointer.NULL) {
            int error = Native.getLastError();
            LOGGER.warn("Failed to map camera view. Error: {}", error);
            csm.kernel32.CloseHandle(csm.fileMapping);
            return null;
        }

        LOGGER.info("Connected to camera shared memory '{}'", MAPPING_NAME);
        return csm;
    }

    private static final int OFFSET_TIMESTAMP = 8;
    private static final int OFFSET_POS_X = 16;
    private static final int OFFSET_POS_Y = 24;
    private static final int OFFSET_POS_Z = 32;
    private static final int OFFSET_X_ROT = 40;
    private static final int OFFSET_Y_ROT = 44;
    private static final int OFFSET_QUAT_X = 48;
    private static final int OFFSET_QUAT_Y = 52;
    private static final int OFFSET_QUAT_Z = 56;
    private static final int OFFSET_QUAT_W = 60;
    private static final int OFFSET_FOV = 64;
    private static final int OFFSET_STATUS = 68;
    private static final int OFFSET_CAMERA_TYPE = 72;
    private static final int OFFSET_DETACHED = 76;

    /**
     * Initialize the shared memory header with magic number and version.
     * Must be called after creating the memory view.
     */
    private void initHeader() {
        if (mappedView == null || mappedView == Pointer.NULL) {
            return;
        }
        // Write magic number "MCCD"
        mappedView.setInt(0, CameraDataHeader.MAGIC);
        // Write version
        mappedView.setInt(4, CameraDataHeader.VERSION);
    }

    /**
     * Write camera data to shared memory.
     */
    public void writeCameraData(Vec3 position, float xRot, float yRot,
                                 Quaternionf rotation, float fov, int cameraType, boolean detached) {
        if (mappedView == null || mappedView == Pointer.NULL) {
            return;
        }

        // Mark as writing
        mappedView.setInt(OFFSET_STATUS, CameraDataHeader.STATUS_WRITING);

        // Write timestamp
        long timestamp = System.nanoTime();
        mappedView.setLong(OFFSET_TIMESTAMP, timestamp);

        // Write position
        mappedView.setLong(OFFSET_POS_X, Double.doubleToLongBits(position.x));
        mappedView.setLong(OFFSET_POS_Y, Double.doubleToLongBits(position.y));
        mappedView.setLong(OFFSET_POS_Z, Double.doubleToLongBits(position.z));

        // Write rotation angles
        mappedView.setInt(OFFSET_X_ROT, Float.floatToIntBits(xRot));
        mappedView.setInt(OFFSET_Y_ROT, Float.floatToIntBits(yRot));

        // Write quaternion
        mappedView.setInt(OFFSET_QUAT_X, Float.floatToIntBits(rotation.x()));
        mappedView.setInt(OFFSET_QUAT_Y, Float.floatToIntBits(rotation.y()));
        mappedView.setInt(OFFSET_QUAT_Z, Float.floatToIntBits(rotation.z()));
        mappedView.setInt(OFFSET_QUAT_W, Float.floatToIntBits(rotation.w()));

        // Write FOV
        mappedView.setInt(OFFSET_FOV, Float.floatToIntBits(fov));

        // Write camera type
        mappedView.setInt(OFFSET_CAMERA_TYPE, cameraType);

        // Write detached status
        mappedView.setInt(OFFSET_DETACHED, detached ? 1 : 0);

        // Mark as ready
        mappedView.setInt(OFFSET_STATUS, CameraDataHeader.STATUS_READY);
    }

    /**
     * Read camera data from shared memory (for client).
     */
    public CameraDataHeader readCameraData() {
        if (mappedView == null || mappedView == Pointer.NULL) {
            return null;
        }

        byte[] data = mappedView.getByteArray(0, CameraDataHeader.SIZE);
        CameraDataHeader header = new CameraDataHeader(data);

        if (!header.isValid()) {
            LOGGER.warn("Invalid camera data header");
            return null;
        }

        return header;
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
        LOGGER.info("Closed camera shared memory");
    }

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

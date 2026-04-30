package com.mshare.screen;

import com.sun.jna.Pointer;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Windows Named Shared Memory for camera data.
 * Creates shared memory accessible across Windows Sessions.
 */
public final class CameraSharedMemory extends BaseSharedMemory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraSharedMemory.class);

    private static final String MAPPING_NAME = "Global\\MCCameraData";

    private CameraSharedMemory() {
        super();
    }

    /**
     * Create shared memory server for camera data.
     */
    public static CameraSharedMemory createServer(long size) {
        CameraSharedMemory csm = new CameraSharedMemory();
        csm.mappedSize = size;
        if (!csm.createServer()) {
            throw new RuntimeException("Failed to create camera shared memory");
        }
        csm.initHeader();
        return csm;
    }

    /**
     * Connect as client to read camera data.
     */
    public static CameraSharedMemory connectClient() {
        CameraSharedMemory csm = new CameraSharedMemory();
        if (!csm.doConnect()) {
            return null;
        }
        return csm;
    }

    @Override
    protected String getMappingName() {
        return MAPPING_NAME;
    }

    @Override
    protected long getSize() {
        return CameraDataHeader.SIZE;
    }

    // Camera data field offsets
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
     */
    private void initHeader() {
        if (mappedView == null || mappedView == Pointer.NULL) {
            return;
        }
        mappedView.setInt(0, CameraDataHeader.MAGIC);
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

        mappedView.setInt(OFFSET_STATUS, CameraDataHeader.STATUS_WRITING);

        long timestamp = System.nanoTime();
        mappedView.setLong(OFFSET_TIMESTAMP, timestamp);

        mappedView.setLong(OFFSET_POS_X, Double.doubleToLongBits(position.x));
        mappedView.setLong(OFFSET_POS_Y, Double.doubleToLongBits(position.y));
        mappedView.setLong(OFFSET_POS_Z, Double.doubleToLongBits(position.z));

        mappedView.setInt(OFFSET_X_ROT, Float.floatToIntBits(xRot));
        mappedView.setInt(OFFSET_Y_ROT, Float.floatToIntBits(yRot));

        mappedView.setInt(OFFSET_QUAT_X, Float.floatToIntBits(rotation.x()));
        mappedView.setInt(OFFSET_QUAT_Y, Float.floatToIntBits(rotation.y()));
        mappedView.setInt(OFFSET_QUAT_Z, Float.floatToIntBits(rotation.z()));
        mappedView.setInt(OFFSET_QUAT_W, Float.floatToIntBits(rotation.w()));

        mappedView.setInt(OFFSET_FOV, Float.floatToIntBits(fov));
        mappedView.setInt(OFFSET_CAMERA_TYPE, cameraType);
        mappedView.setInt(OFFSET_DETACHED, detached ? 1 : 0);

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
        CameraDataHeader header = CameraDataHeader.wrap(data);

        if (!header.isValid()) {
            LOGGER.warn("Invalid camera data header");
            return null;
        }

        return header;
    }
}

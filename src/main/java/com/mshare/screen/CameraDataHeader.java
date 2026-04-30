package com.mshare.screen;

/**
 * Camera data structure for shared memory.
 *
 * Layout (128 bytes total):
 *   0  magic:        4 bytes  - Magic number "MCCD"
 *   4  version:       4 bytes  - Protocol version (1)
 *   8  timestamp:    8 bytes  - Timestamp in nanoseconds
 *  16  pos_x:        8 bytes  - Camera X position (double)
 *  24  pos_y:        8 bytes  - Camera Y position (double)
 *  32  pos_z:        8 bytes  - Camera Z position (double)
 *  40  xRot:         4 bytes  - Pitch angle in degrees
 *  44  yRot:         4 bytes  - Yaw angle in degrees
 *  48  quat_x:       4 bytes  - Quaternion X component
 *  52  quat_y:       4 bytes  - Quaternion Y component
 *  56  quat_z:       4 bytes  - Quaternion Z component
 *  60  quat_w:       4 bytes  - Quaternion W component
 *  64  fov:          4 bytes  - Field of view in degrees
 *  68  status:       4 bytes  - Status: 0=idle, 1=writing, 2=ready
 *  72  cameraType:   4 bytes  - Camera type: 0=first_person, 1=third_back, 2=third_front
 *  76  isDetached:   4 bytes  - Is detached (third person)
 *  80  reserved:    48 bytes  - Reserved for future use
 */
public final class CameraDataHeader extends BinaryStruct {
    public static final int SIZE = 128;
    public static final int MAGIC = 0x4D434344; // "MCCD" in ASCII
    public static final int VERSION = 1;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WRITING = 1;
    public static final int STATUS_READY = 2;

    public static final int CAMERA_FIRST_PERSON = 0;
    public static final int CAMERA_THIRD_BACK = 1;
    public static final int CAMERA_THIRD_FRONT = 2;

    private CameraDataHeader(byte[] data) {
        super(data);
    }

    public static CameraDataHeader create() {
        byte[] data = new byte[SIZE];
        CameraDataHeader header = new CameraDataHeader(data);
        header.setMagic(MAGIC);
        header.setVersion(VERSION);
        header.setTimestamp(0L);
        header.setPosition(0.0, 0.0, 0.0);
        header.setRotation(0.0f, 0.0f);
        header.setQuaternion(0.0f, 0.0f, 0.0f, 1.0f);
        header.setFov(70.0f);
        header.setStatus(STATUS_IDLE);
        header.setCameraType(CAMERA_FIRST_PERSON);
        header.setDetached(false);
        return header;
    }

    public static CameraDataHeader wrap(byte[] data) {
        if (data.length < SIZE) {
            throw new IllegalArgumentException("Buffer too small for CameraDataHeader");
        }
        return new CameraDataHeader(data);
    }

    public byte[] getBytes() {
        return data;
    }

    public boolean isValid() {
        return readInt(0) == MAGIC;
    }

    // Getters
    public int getMagic() { return readInt(0); }
    public int getVersion() { return readInt(4); }
    public long getTimestamp() { return readLong(8); }
    public double getPosX() { return readDouble(16); }
    public double getPosY() { return readDouble(24); }
    public double getPosZ() { return readDouble(32); }
    public float getXRot() { return readFloat(40); }
    public float getYRot() { return readFloat(44); }
    public float getQuatX() { return readFloat(48); }
    public float getQuatY() { return readFloat(52); }
    public float getQuatZ() { return readFloat(56); }
    public float getQuatW() { return readFloat(60); }
    public float getFov() { return readFloat(64); }
    public int getStatus() { return readInt(68); }
    public int getCameraType() { return readInt(72); }
    public boolean isDetached() { return readInt(76) != 0; }

    // Setters
    public void setMagic(int magic) { writeInt(0, magic); }
    public void setVersion(int version) { writeInt(4, version); }
    public void setTimestamp(long timestamp) { writeLong(8, timestamp); }
    public void setPosX(double x) { writeDouble(16, x); }
    public void setPosY(double y) { writeDouble(24, y); }
    public void setPosZ(double z) { writeDouble(32, z); }
    public void setPosition(double x, double y, double z) {
        writeDouble(16, x);
        writeDouble(24, y);
        writeDouble(32, z);
    }
    public void setXRot(float xRot) { writeFloat(40, xRot); }
    public void setYRot(float yRot) { writeFloat(44, yRot); }
    public void setRotation(float xRot, float yRot) {
        writeFloat(40, xRot);
        writeFloat(44, yRot);
    }
    public void setQuaternion(float x, float y, float z, float w) {
        writeFloat(48, x);
        writeFloat(52, y);
        writeFloat(56, z);
        writeFloat(60, w);
    }
    public void setFov(float fov) { writeFloat(64, fov); }
    public void setStatus(int status) { writeInt(68, status); }
    public void setCameraType(int cameraType) { writeInt(72, cameraType); }
    public void setDetached(boolean detached) { writeInt(76, detached ? 1 : 0); }

    @Override
    public String toString() {
        return "CameraDataHeader{" +
                "valid=" + isValid() +
                ", version=" + getVersion() +
                ", pos=(" + getPosX() + ", " + getPosY() + ", " + getPosZ() + ")" +
                ", pitch=" + getXRot() +
                ", yaw=" + getYRot() +
                ", fov=" + getFov() +
                ", status=" + getStatus() +
                ", cameraType=" + getCameraType() +
                ", detached=" + isDetached() +
                '}';
    }
}

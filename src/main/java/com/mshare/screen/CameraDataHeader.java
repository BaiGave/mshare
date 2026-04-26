package com.mshare.screen;

/**
 * Camera data structure for shared memory.
 * Layout (128 bytes total):
 * - magic:        4 bytes  - Magic number "MCCD"
 * - version:      4 bytes  - Protocol version (1)
 * - timestamp:    8 bytes  - Timestamp in nanoseconds
 * - pos_x:        8 bytes  - Camera X position (double)
 * - pos_y:         8 bytes  - Camera Y position (double)
 * - pos_z:         8 bytes  - Camera Z position (double)
 * - xRot:         4 bytes  - Pitch angle in degrees
 * - yRot:         4 bytes  - Yaw angle in degrees
 * - quat_x:       4 bytes  - Quaternion X component
 * - quat_y:       4 bytes  - Quaternion Y component
 * - quat_z:       4 bytes  - Quaternion Z component
 * - quat_w:       4 bytes  - Quaternion W component
 * - fov:          4 bytes  - Field of view in degrees
 * - status:       4 bytes  - Status: 0=idle, 1=writing, 2=ready
 * - cameraType:   4 bytes  - Camera type: 0=first_person, 1=third_back, 2=third_front
 * - isDetached:   4 bytes  - Is detached (third person)
 * - reserved:    41 bytes  - Reserved for future use
 */
public final class CameraDataHeader {
    public static final int SIZE = 128;
    public static final int MAGIC = 0x4D434344; // "MCCD" in ASCII
    public static final int VERSION = 1;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WRITING = 1;
    public static final int STATUS_READY = 2;

    public static final int CAMERA_FIRST_PERSON = 0;
    public static final int CAMERA_THIRD_BACK = 1;
    public static final int CAMERA_THIRD_FRONT = 2;

    private final byte[] data;

    public CameraDataHeader() {
        this.data = new byte[SIZE];
        setMagic(MAGIC);
        setVersion(VERSION);
        setTimestamp(0L);
        setPosition(0.0, 0.0, 0.0);
        setRotation(0.0f, 0.0f);
        setQuaternion(0.0f, 0.0f, 0.0f, 1.0f);
        setFov(70.0f);
        setStatus(STATUS_IDLE);
        setCameraType(CAMERA_FIRST_PERSON);
        setDetached(false);
    }

    public CameraDataHeader(byte[] data) {
        if (data.length < SIZE) {
            throw new IllegalArgumentException("Buffer too small for CameraDataHeader");
        }
        this.data = data;
    }

    public byte[] getBytes() {
        return data;
    }

    public int getMagic() {
        return readInt(0);
    }

    public void setMagic(int magic) {
        writeInt(0, magic);
    }

    public boolean isValid() {
        return getMagic() == MAGIC;
    }

    public int getVersion() {
        return readInt(4);
    }

    public void setVersion(int version) {
        writeInt(4, version);
    }

    public long getTimestamp() {
        return readLong(8);
    }

    public void setTimestamp(long timestamp) {
        writeLong(8, timestamp);
    }

    public double getPosX() {
        return readDouble(16);
    }

    public double getPosY() {
        return readDouble(24);
    }

    public double getPosZ() {
        return readDouble(32);
    }

    public void setPosition(double x, double y, double z) {
        writeDouble(16, x);
        writeDouble(24, y);
        writeDouble(32, z);
    }

    public float getXRot() {
        return readFloat(40);
    }

    public void setXRot(float xRot) {
        writeFloat(40, xRot);
    }

    public float getYRot() {
        return readFloat(44);
    }

    public void setYRot(float yRot) {
        writeFloat(44, yRot);
    }

    public void setRotation(float xRot, float yRot) {
        setXRot(xRot);
        setYRot(yRot);
    }

    public float getQuatX() {
        return readFloat(48);
    }

    public float getQuatY() {
        return readFloat(52);
    }

    public float getQuatZ() {
        return readFloat(56);
    }

    public float getQuatW() {
        return readFloat(60);
    }

    public void setQuaternion(float x, float y, float z, float w) {
        writeFloat(48, x);
        writeFloat(52, y);
        writeFloat(56, z);
        writeFloat(60, w);
    }

    public float getFov() {
        return readFloat(64);
    }

    public void setFov(float fov) {
        writeFloat(64, fov);
    }

    public int getStatus() {
        return readInt(68);
    }

    public void setStatus(int status) {
        writeInt(68, status);
    }

    public int getCameraType() {
        return readInt(72);
    }

    public void setCameraType(int cameraType) {
        writeInt(72, cameraType);
    }

    public boolean isDetached() {
        return readInt(76) != 0;
    }

    public void setDetached(boolean detached) {
        writeInt(76, detached ? 1 : 0);
    }

    private int readInt(int offset) {
        return ((data[offset] & 0xFF))
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    private void writeInt(int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private long readLong(int offset) {
        return ((long) data[offset] & 0xFF)
                | (((long) data[offset + 1] & 0xFF) << 8)
                | (((long) data[offset + 2] & 0xFF) << 16)
                | (((long) data[offset + 3] & 0xFF) << 24)
                | (((long) data[offset + 4] & 0xFF) << 32)
                | (((long) data[offset + 5] & 0xFF) << 40)
                | (((long) data[offset + 6] & 0xFF) << 48)
                | (((long) data[offset + 7] & 0xFF) << 56);
    }

    private void writeLong(int offset, long value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
        data[offset + 4] = (byte) ((value >> 32) & 0xFF);
        data[offset + 5] = (byte) ((value >> 40) & 0xFF);
        data[offset + 6] = (byte) ((value >> 48) & 0xFF);
        data[offset + 7] = (byte) ((value >> 56) & 0xFF);
    }

    private double readDouble(int offset) {
        return Double.longBitsToDouble(readLong(offset));
    }

    private void writeDouble(int offset, double value) {
        writeLong(offset, Double.doubleToLongBits(value));
    }

    private float readFloat(int offset) {
        return Float.intBitsToFloat(readInt(offset));
    }

    private void writeFloat(int offset, float value) {
        writeInt(offset, Float.floatToIntBits(value));
    }

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

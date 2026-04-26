package com.mshare.screen;

/**
 * Shared memory file header structure.
 * This header is mapped at the beginning of the Named Shared Memory file,
 * followed by the raw pixel data.
 *
 * Layout (64 bytes total):
 * - magic:        4 bytes  - Magic number "MCSH"
 * - version:      4 bytes  - Protocol version (1)
 * - width:        4 bytes  - Screen width in pixels
 * - height:       4 bytes  - Screen height in pixels
 * - format:       4 bytes  - Pixel format (0=RGB24, 1=RGBA32)
 * - stride:       4 bytes  - Bytes per row
 * - timestamp:    8 bytes  - Timestamp in nanoseconds (System.nanoTime())
 * - frameCount:   4 bytes  - Frame counter
 * - status:       4 bytes  - Status: 0=idle, 1=writing, 2=ready
 * - reserved:    24 bytes  - Reserved for future use
 */
public final class ScreenHeader {
    public static final int SIZE = 64;
    public static final int MAGIC = 0x4D435348; // "MCSH" in ASCII
    public static final int VERSION = 1;

    // Pixel formats
    public static final int FORMAT_RGB24 = 0;
    public static final int FORMAT_RGBA32 = 1;

    // Status flags
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WRITING = 1;
    public static final int STATUS_READY = 2;

    private final byte[] data;

    public ScreenHeader() {
        this.data = new byte[SIZE];
        setMagic(MAGIC);
        setVersion(VERSION);
        setWidth(0);
        setHeight(0);
        setFormat(FORMAT_RGB24);
        setStride(0);
        setTimestamp(0L);
        setFrameCount(0L);
        setStatus(STATUS_IDLE);
    }

    public ScreenHeader(byte[] data) {
        if (data.length < SIZE) {
            throw new IllegalArgumentException("Buffer too small for ScreenHeader");
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

    public int getWidth() {
        return readInt(8);
    }

    public void setWidth(int width) {
        writeInt(8, width);
    }

    public int getHeight() {
        return readInt(12);
    }

    public void setHeight(int height) {
        writeInt(12, height);
    }

    public int getFormat() {
        return readInt(16);
    }

    public void setFormat(int format) {
        writeInt(16, format);
    }

    public int getStride() {
        return readInt(20);
    }

    public void setStride(int stride) {
        writeInt(20, stride);
    }

    public long getTimestamp() {
        return readLong(24);
    }

    public void setTimestamp(long timestamp) {
        writeLong(24, timestamp);
    }

    public long getFrameCount() {
        return readLong(32);
    }

    public void setFrameCount(long frameCount) {
        writeLong(32, frameCount);
    }

    public int getStatus() {
        return readInt(40);
    }

    public void setStatus(int status) {
        writeInt(40, status);
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

    public int getPixelDataSize() {
        int width = getWidth();
        int height = getHeight();
        int bpp = (getFormat() == FORMAT_RGBA32) ? 4 : 3;
        return width * height * bpp;
    }

    public int getTotalSize() {
        return SIZE + getPixelDataSize();
    }

    @Override
    public String toString() {
        return "ScreenHeader{" +
                "valid=" + isValid() +
                ", version=" + getVersion() +
                ", width=" + getWidth() +
                ", height=" + getHeight() +
                ", format=" + getFormat() +
                ", stride=" + getStride() +
                ", timestamp=" + getTimestamp() +
                ", frameCount=" + getFrameCount() +
                ", status=" + getStatus() +
                '}';
    }
}

package com.mshare.screen;

/**
 * Screen capture shared memory header structure.
 *
 * Layout (64 bytes total, v2):
 *   0  magic:        4 bytes  - Magic number "MCSH"
 *   4  version:       4 bytes  - Protocol version (2)
 *   8  screenWidth:  4 bytes  - Actual window framebuffer width
 *  12  screenHeight: 4 bytes  - Actual window framebuffer height
 *  16  width:        4 bytes  - Capture width (after downscale)
 *  20  height:       4 bytes  - Capture height (after downscale)
 *  24  format:       4 bytes  - Pixel format (1=RGBA32)
 *  28  stride:       4 bytes  - Bytes per row
 *  32  reserved:     4 bytes  - Reserved
 *  36  timestamp:    8 bytes  - Timestamp in nanoseconds
 *  44  frameCount:  8 bytes  - Frame counter
 *  52  status:       4 bytes  - Status: 0=idle, 1=writing, 2=ready
 *  56  reserved:      8 bytes  - Reserved
 *  64  pixel data
 */
public final class ScreenHeader extends BinaryStruct {
    public static final int SIZE = 64;
    public static final int MAGIC = 0x4D435348; // "MCSH" in ASCII
    public static final int VERSION = 2;

    public static final int FORMAT_RGB24 = 0;
    public static final int FORMAT_RGBA32 = 1;

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WRITING = 1;
    public static final int STATUS_READY = 2;

    private ScreenHeader(byte[] data) {
        super(data);
    }

    public static ScreenHeader create() {
        byte[] data = new byte[SIZE];
        ScreenHeader header = new ScreenHeader(data);
        header.setMagic(MAGIC);
        header.setVersion(VERSION);
        return header;
    }

    public static ScreenHeader wrap(byte[] data) {
        if (data.length < SIZE) {
            throw new IllegalArgumentException("Buffer too small for ScreenHeader");
        }
        return new ScreenHeader(data);
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
    public int getScreenWidth() { return readInt(8); }
    public int getScreenHeight() { return readInt(12); }
    public int getWidth() { return readInt(16); }
    public int getHeight() { return readInt(20); }
    public int getFormat() { return readInt(24); }
    public int getStride() { return readInt(28); }
    public long getTimestamp() { return readLong(36); }
    public long getFrameCount() { return readLong(44); }
    public int getStatus() { return readInt(52); }

    // Setters
    public void setMagic(int magic) { writeInt(0, magic); }
    public void setVersion(int version) { writeInt(4, version); }
    public void setScreenWidth(int width) { writeInt(8, width); }
    public void setScreenHeight(int height) { writeInt(12, height); }
    public void setWidth(int width) { writeInt(16, width); }
    public void setHeight(int height) { writeInt(20, height); }
    public void setFormat(int format) { writeInt(24, format); }
    public void setStride(int stride) { writeInt(28, stride); }
    public void setTimestamp(long timestamp) { writeLong(36, timestamp); }
    public void setFrameCount(long frameCount) { writeLong(44, frameCount); }
    public void setStatus(int status) { writeInt(52, status); }

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
                ", screen=" + getScreenWidth() + "x" + getScreenHeight() +
                ", capture=" + getWidth() + "x" + getHeight() +
                ", format=" + getFormat() +
                ", stride=" + getStride() +
                ", timestamp=" + getTimestamp() +
                ", frameCount=" + getFrameCount() +
                ", status=" + getStatus() +
                '}';
    }
}

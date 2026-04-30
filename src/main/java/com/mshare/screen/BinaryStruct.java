package com.mshare.screen;

/**
 * Abstract base class for binary data structures stored in shared memory.
 * Provides common methods for reading and writing primitive types in little-endian format.
 *
 * Subclasses must:
 * - Define the data buffer
 * - Implement getOffset() for structure-specific offsets
 */
public abstract class BinaryStruct {
    protected final byte[] data;

    protected BinaryStruct(byte[] data) {
        this.data = data;
    }

    protected int readInt(int offset) {
        return ((data[offset] & 0xFF))
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    protected void writeInt(int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    protected long readLong(int offset) {
        return ((long) data[offset] & 0xFF)
                | (((long) data[offset + 1] & 0xFF) << 8)
                | (((long) data[offset + 2] & 0xFF) << 16)
                | (((long) data[offset + 3] & 0xFF) << 24)
                | (((long) data[offset + 4] & 0xFF) << 32)
                | (((long) data[offset + 5] & 0xFF) << 40)
                | (((long) data[offset + 6] & 0xFF) << 48)
                | (((long) data[offset + 7] & 0xFF) << 56);
    }

    protected void writeLong(int offset, long value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
        data[offset + 4] = (byte) ((value >> 32) & 0xFF);
        data[offset + 5] = (byte) ((value >> 40) & 0xFF);
        data[offset + 6] = (byte) ((value >> 48) & 0xFF);
        data[offset + 7] = (byte) ((value >> 56) & 0xFF);
    }

    protected float readFloat(int offset) {
        return Float.intBitsToFloat(readInt(offset));
    }

    protected void writeFloat(int offset, float value) {
        writeInt(offset, Float.floatToIntBits(value));
    }

    protected double readDouble(int offset) {
        return Double.longBitsToDouble(readLong(offset));
    }

    protected void writeDouble(int offset, double value) {
        writeLong(offset, Double.doubleToLongBits(value));
    }

    protected void readBytes(int offset, byte[] dest, int destOffset, int length) {
        System.arraycopy(data, offset, dest, destOffset, length);
    }

    protected void writeBytes(int offset, byte[] src, int srcOffset, int length) {
        System.arraycopy(src, srcOffset, data, offset, length);
    }
}

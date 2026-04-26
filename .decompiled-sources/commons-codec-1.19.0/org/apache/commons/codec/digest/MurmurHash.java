/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.digest;

final class MurmurHash {
    MurmurHash() {
    }

    static int getLittleEndianInt(byte[] data, int index) {
        return data[index] & 0xFF | (data[index + 1] & 0xFF) << 8 | (data[index + 2] & 0xFF) << 16 | (data[index + 3] & 0xFF) << 24;
    }

    static long getLittleEndianLong(byte[] data, int index) {
        return (long)data[index] & 0xFFL | ((long)data[index + 1] & 0xFFL) << 8 | ((long)data[index + 2] & 0xFFL) << 16 | ((long)data[index + 3] & 0xFFL) << 24 | ((long)data[index + 4] & 0xFFL) << 32 | ((long)data[index + 5] & 0xFFL) << 40 | ((long)data[index + 6] & 0xFFL) << 48 | ((long)data[index + 7] & 0xFFL) << 56;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ZipEightByteInteger
implements Serializable {
    static final int BYTES = 8;
    private static final long serialVersionUID = 1L;
    public static final ZipEightByteInteger ZERO = new ZipEightByteInteger(0L);
    private static final BigInteger HIGHEST_BIT = BigInteger.ONE.shiftLeft(63);
    private final long value;

    public static byte[] getBytes(BigInteger value) {
        return ZipEightByteInteger.getBytes(value.longValue());
    }

    public static byte[] getBytes(long value) {
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
    }

    public static long getLongValue(byte[] bytes) {
        return ZipEightByteInteger.getLongValue(bytes, 0);
    }

    public static long getLongValue(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong(offset);
    }

    public static BigInteger getValue(byte[] bytes) {
        return ZipEightByteInteger.getValue(bytes, 0);
    }

    public static BigInteger getValue(byte[] bytes, int offset) {
        return ZipEightByteInteger.toUnsignedBigInteger(ZipEightByteInteger.getLongValue(bytes, offset));
    }

    static BigInteger toUnsignedBigInteger(long value) {
        if (value >= 0L) {
            return BigInteger.valueOf(value);
        }
        return BigInteger.valueOf(value & Long.MAX_VALUE).add(HIGHEST_BIT);
    }

    public ZipEightByteInteger(BigInteger value) {
        this.value = value.longValue();
    }

    public ZipEightByteInteger(byte[] bytes) {
        this(bytes, 0);
    }

    public ZipEightByteInteger(byte[] bytes, int offset) {
        this.value = ZipEightByteInteger.getLongValue(bytes, offset);
    }

    public ZipEightByteInteger(long value) {
        this.value = value;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ZipEightByteInteger)) {
            return false;
        }
        return this.value == ((ZipEightByteInteger)o).value;
    }

    public byte[] getBytes() {
        return ZipEightByteInteger.getBytes(this.value);
    }

    public long getLongValue() {
        return this.value;
    }

    public BigInteger getValue() {
        return ZipEightByteInteger.toUnsignedBigInteger(this.value);
    }

    public int hashCode() {
        return Long.hashCode(this.value);
    }

    public String toString() {
        return Long.toUnsignedString(this.value);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.primitives;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedLongs;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.math.BigInteger;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class UnsignedLong
extends Number
implements Comparable<UnsignedLong> {
    private static final long UNSIGNED_MASK = Long.MAX_VALUE;
    public static final UnsignedLong ZERO = new UnsignedLong(0L);
    public static final UnsignedLong ONE = new UnsignedLong(1L);
    public static final UnsignedLong MAX_VALUE = new UnsignedLong(-1L);
    private final long value;

    private UnsignedLong(long value) {
        this.value = value;
    }

    public static UnsignedLong fromLongBits(long bits) {
        return new UnsignedLong(bits);
    }

    @CanIgnoreReturnValue
    public static UnsignedLong valueOf(long value) {
        Preconditions.checkArgument(value >= 0L, "value (%s) is outside the range for an unsigned long value", value);
        return UnsignedLong.fromLongBits(value);
    }

    @CanIgnoreReturnValue
    public static UnsignedLong valueOf(BigInteger value) {
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(value.signum() >= 0 && value.bitLength() <= 64, "value (%s) is outside the range for an unsigned long value", (Object)value);
        return UnsignedLong.fromLongBits(value.longValue());
    }

    @CanIgnoreReturnValue
    public static UnsignedLong valueOf(String string) {
        return UnsignedLong.valueOf(string, 10);
    }

    @CanIgnoreReturnValue
    public static UnsignedLong valueOf(String string, int radix) {
        return UnsignedLong.fromLongBits(UnsignedLongs.parseUnsignedLong(string, radix));
    }

    public UnsignedLong plus(UnsignedLong val) {
        return UnsignedLong.fromLongBits(this.value + Preconditions.checkNotNull(val).value);
    }

    public UnsignedLong minus(UnsignedLong val) {
        return UnsignedLong.fromLongBits(this.value - Preconditions.checkNotNull(val).value);
    }

    public UnsignedLong times(UnsignedLong val) {
        return UnsignedLong.fromLongBits(this.value * Preconditions.checkNotNull(val).value);
    }

    public UnsignedLong dividedBy(UnsignedLong val) {
        return UnsignedLong.fromLongBits(UnsignedLongs.divide(this.value, Preconditions.checkNotNull(val).value));
    }

    public UnsignedLong mod(UnsignedLong val) {
        return UnsignedLong.fromLongBits(UnsignedLongs.remainder(this.value, Preconditions.checkNotNull(val).value));
    }

    @Override
    public int intValue() {
        return (int)this.value;
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        if (this.value >= 0L) {
            return this.value;
        }
        return (float)(this.value >>> 1 | this.value & 1L) * 2.0f;
    }

    @Override
    public double doubleValue() {
        if (this.value >= 0L) {
            return this.value;
        }
        return (double)(this.value >>> 1 | this.value & 1L) * 2.0;
    }

    public BigInteger bigIntegerValue() {
        BigInteger bigInt = BigInteger.valueOf(this.value & Long.MAX_VALUE);
        if (this.value < 0L) {
            bigInt = bigInt.setBit(63);
        }
        return bigInt;
    }

    @Override
    public int compareTo(UnsignedLong o) {
        Preconditions.checkNotNull(o);
        return UnsignedLongs.compare(this.value, o.value);
    }

    public int hashCode() {
        return Long.hashCode(this.value);
    }

    public boolean equals(@Nullable Object obj) {
        if (obj instanceof UnsignedLong) {
            UnsignedLong other = (UnsignedLong)obj;
            return this.value == other.value;
        }
        return false;
    }

    public String toString() {
        return UnsignedLongs.toString(this.value);
    }

    public String toString(int radix) {
        return UnsignedLongs.toString(this.value, radix);
    }
}


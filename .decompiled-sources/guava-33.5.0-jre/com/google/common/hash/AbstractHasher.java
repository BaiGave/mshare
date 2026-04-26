/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hasher;
import com.google.common.hash.Java8Compatibility;
import com.google.common.hash.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

abstract class AbstractHasher
implements Hasher {
    AbstractHasher() {
    }

    @Override
    @CanIgnoreReturnValue
    public final Hasher putBoolean(boolean b) {
        return this.putByte(b ? (byte)1 : 0);
    }

    @Override
    @CanIgnoreReturnValue
    public final Hasher putDouble(double d) {
        return this.putLong(Double.doubleToRawLongBits(d));
    }

    @Override
    @CanIgnoreReturnValue
    public final Hasher putFloat(float f) {
        return this.putInt(Float.floatToRawIntBits(f));
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putUnencodedChars(CharSequence charSequence) {
        int len = charSequence.length();
        for (int i = 0; i < len; ++i) {
            this.putChar(charSequence.charAt(i));
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putString(CharSequence charSequence, Charset charset) {
        return this.putBytes(charSequence.toString().getBytes(charset));
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(byte[] bytes) {
        return this.putBytes(bytes, 0, bytes.length);
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(byte[] bytes, int off, int len) {
        Preconditions.checkPositionIndexes(off, off + len, bytes.length);
        for (int i = 0; i < len; ++i) {
            this.putByte(bytes[off + i]);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(ByteBuffer b) {
        if (b.hasArray()) {
            this.putBytes(b.array(), b.arrayOffset() + b.position(), b.remaining());
            Java8Compatibility.position(b, b.limit());
        } else {
            for (int remaining = b.remaining(); remaining > 0; --remaining) {
                this.putByte(b.get());
            }
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putShort(short s) {
        this.putByte((byte)s);
        this.putByte((byte)(s >>> 8));
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putInt(int i) {
        this.putByte((byte)i);
        this.putByte((byte)(i >>> 8));
        this.putByte((byte)(i >>> 16));
        this.putByte((byte)(i >>> 24));
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putLong(long l) {
        for (int i = 0; i < 64; i += 8) {
            this.putByte((byte)(l >>> i));
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Hasher putChar(char c) {
        this.putByte((byte)c);
        this.putByte((byte)(c >>> 8));
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public <T> Hasher putObject(@ParametricNullness T instance, Funnel<? super T> funnel) {
        funnel.funnel(instance, this);
        return this;
    }
}


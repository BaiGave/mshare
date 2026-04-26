/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Beta
public interface PrimitiveSink {
    @CanIgnoreReturnValue
    public PrimitiveSink putByte(byte var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putBytes(byte[] var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putBytes(byte[] var1, int var2, int var3);

    @CanIgnoreReturnValue
    public PrimitiveSink putBytes(ByteBuffer var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putShort(short var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putInt(int var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putLong(long var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putFloat(float var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putDouble(double var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putBoolean(boolean var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putChar(char var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putUnencodedChars(CharSequence var1);

    @CanIgnoreReturnValue
    public PrimitiveSink putString(CharSequence var1, Charset var2);
}


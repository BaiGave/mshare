/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.annotations.Beta;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.ParametricNullness;
import com.google.common.hash.PrimitiveSink;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Beta
public interface Hasher
extends PrimitiveSink {
    @Override
    @CanIgnoreReturnValue
    public Hasher putByte(byte var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(byte[] var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(byte[] var1, int var2, int var3);

    @Override
    @CanIgnoreReturnValue
    public Hasher putBytes(ByteBuffer var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putShort(short var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putInt(int var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putLong(long var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putFloat(float var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putDouble(double var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putBoolean(boolean var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putChar(char var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putUnencodedChars(CharSequence var1);

    @Override
    @CanIgnoreReturnValue
    public Hasher putString(CharSequence var1, Charset var2);

    @CanIgnoreReturnValue
    public <T> Hasher putObject(@ParametricNullness T var1, Funnel<? super T> var2);

    public HashCode hash();

    @Deprecated
    public int hashCode();
}


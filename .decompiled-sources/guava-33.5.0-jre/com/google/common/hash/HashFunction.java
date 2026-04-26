/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.ParametricNullness;
import com.google.errorprone.annotations.Immutable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Immutable
public interface HashFunction {
    public Hasher newHasher();

    public Hasher newHasher(int var1);

    public HashCode hashInt(int var1);

    public HashCode hashLong(long var1);

    public HashCode hashBytes(byte[] var1);

    public HashCode hashBytes(byte[] var1, int var2, int var3);

    public HashCode hashBytes(ByteBuffer var1);

    public HashCode hashUnencodedChars(CharSequence var1);

    public HashCode hashString(CharSequence var1, Charset var2);

    public <T> HashCode hashObject(@ParametricNullness T var1, Funnel<? super T> var2);

    public int bits();
}


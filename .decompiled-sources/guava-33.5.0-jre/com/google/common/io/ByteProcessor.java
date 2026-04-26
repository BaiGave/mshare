/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.io.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.io.IOException;

@DoNotMock(value="Implement it normally")
@J2ktIncompatible
@GwtIncompatible
public interface ByteProcessor<T> {
    @CanIgnoreReturnValue
    public boolean processBytes(byte[] var1, int var2, int var3) throws IOException;

    @ParametricNullness
    public T getResult();
}


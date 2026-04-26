/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.io.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;

@J2ktIncompatible
@GwtIncompatible
public interface LineProcessor<T> {
    @CanIgnoreReturnValue
    public boolean processLine(String var1) throws IOException;

    @ParametricNullness
    public T getResult();
}


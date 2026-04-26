/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@Deprecated
@GwtCompatible
public class ComputationException
extends RuntimeException {
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    public ComputationException(@Nullable Throwable cause) {
        super(cause);
    }
}


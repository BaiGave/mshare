/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public class UncheckedExecutionException
extends RuntimeException {
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    @Deprecated
    protected UncheckedExecutionException() {
    }

    @Deprecated
    protected UncheckedExecutionException(@Nullable String message) {
        super(message);
    }

    public UncheckedExecutionException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public UncheckedExecutionException(@Nullable Throwable cause) {
        super(cause);
    }
}


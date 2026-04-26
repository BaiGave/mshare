/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public class ExecutionError
extends Error {
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    @Deprecated
    protected ExecutionError() {
    }

    @Deprecated
    protected ExecutionError(@Nullable String message) {
        super(message);
    }

    public ExecutionError(@Nullable String message, @Nullable Error cause) {
        super(message, cause);
    }

    public ExecutionError(@Nullable Error cause) {
        super(cause);
    }
}


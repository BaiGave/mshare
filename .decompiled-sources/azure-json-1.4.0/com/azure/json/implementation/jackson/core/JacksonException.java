/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonLocation;
import java.io.IOException;

public abstract class JacksonException
extends IOException {
    private static final long serialVersionUID = 123L;

    protected JacksonException(String msg) {
        super(msg);
    }

    protected JacksonException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

    public abstract JsonLocation getLocation();

    public abstract Object getProcessor();
}


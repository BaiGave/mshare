/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.exc.StreamWriteException;

public class JsonGenerationException
extends StreamWriteException {
    private static final long serialVersionUID = 123L;

    @Deprecated
    public JsonGenerationException(Throwable rootCause) {
        super(rootCause, null);
    }

    @Deprecated
    public JsonGenerationException(String msg) {
        super(msg, (JsonGenerator)null);
    }

    @Deprecated
    public JsonGenerationException(String msg, Throwable rootCause) {
        super(msg, rootCause, null);
    }

    public JsonGenerationException(String msg, JsonGenerator g) {
        super(msg, g);
        this._processor = g;
    }

    @Override
    public JsonGenerator getProcessor() {
        return this._processor;
    }
}


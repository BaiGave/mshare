/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.exc;

import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.JsonLocation;
import com.azure.json.implementation.jackson.core.JsonProcessingException;

public abstract class StreamWriteException
extends JsonProcessingException {
    private static final long serialVersionUID = 2L;
    protected transient JsonGenerator _processor;

    protected StreamWriteException(Throwable rootCause, JsonGenerator g) {
        super(rootCause);
        this._processor = g;
    }

    protected StreamWriteException(String msg, JsonGenerator g) {
        super(msg, (JsonLocation)null);
        this._processor = g;
    }

    protected StreamWriteException(String msg, Throwable rootCause, JsonGenerator g) {
        super(msg, null, rootCause);
        this._processor = g;
    }

    @Override
    public JsonGenerator getProcessor() {
        return this._processor;
    }
}


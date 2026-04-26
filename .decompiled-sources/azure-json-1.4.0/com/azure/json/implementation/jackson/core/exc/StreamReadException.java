/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.exc;

import com.azure.json.implementation.jackson.core.JsonLocation;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.azure.json.implementation.jackson.core.util.RequestPayload;

public abstract class StreamReadException
extends JsonProcessingException {
    static final long serialVersionUID = 2L;
    protected transient JsonParser _processor;
    protected RequestPayload _requestPayload;

    protected StreamReadException(JsonParser p, String msg) {
        super(msg, p == null ? null : p.getCurrentLocation());
        this._processor = p;
    }

    protected StreamReadException(JsonParser p, String msg, Throwable root) {
        super(msg, p == null ? null : p.getCurrentLocation(), root);
        this._processor = p;
    }

    protected StreamReadException(JsonParser p, String msg, JsonLocation loc) {
        super(msg, loc, null);
        this._processor = p;
    }

    protected StreamReadException(String msg, JsonLocation loc, Throwable rootCause) {
        super(msg, loc, rootCause);
    }

    public abstract StreamReadException withRequestPayload(RequestPayload var1);

    @Override
    public JsonParser getProcessor() {
        return this._processor;
    }

    public RequestPayload getRequestPayload() {
        return this._requestPayload;
    }

    public String getRequestPayloadAsString() {
        return this._requestPayload != null ? this._requestPayload.toString() : null;
    }

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (this._requestPayload != null) {
            msg = msg + "\nRequest payload : " + this._requestPayload;
        }
        return msg;
    }
}


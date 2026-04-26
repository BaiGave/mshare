/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonLocation;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.exc.StreamReadException;
import com.azure.json.implementation.jackson.core.util.RequestPayload;

public class JsonParseException
extends StreamReadException {
    private static final long serialVersionUID = 2L;

    @Deprecated
    public JsonParseException(String msg, JsonLocation loc) {
        super(msg, loc, null);
    }

    @Deprecated
    public JsonParseException(String msg, JsonLocation loc, Throwable root) {
        super(msg, loc, root);
    }

    public JsonParseException(JsonParser p, String msg) {
        super(p, msg);
    }

    public JsonParseException(JsonParser p, String msg, Throwable root) {
        super(p, msg, root);
    }

    public JsonParseException(JsonParser p, String msg, JsonLocation loc) {
        super(p, msg, loc);
    }

    public JsonParseException(JsonParser p, String msg, JsonLocation loc, Throwable root) {
        super(msg, loc, root);
    }

    @Override
    public JsonParseException withRequestPayload(RequestPayload payload) {
        this._requestPayload = payload;
        return this;
    }

    @Override
    public JsonParser getProcessor() {
        return super.getProcessor();
    }

    @Override
    public RequestPayload getRequestPayload() {
        return super.getRequestPayload();
    }

    @Override
    public String getRequestPayloadAsString() {
        return super.getRequestPayloadAsString();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}


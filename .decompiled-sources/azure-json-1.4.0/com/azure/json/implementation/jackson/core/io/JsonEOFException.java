/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.io;

import com.azure.json.implementation.jackson.core.JsonParseException;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.JsonToken;

public class JsonEOFException
extends JsonParseException {
    private static final long serialVersionUID = 1L;
    protected final JsonToken _token;

    public JsonEOFException(JsonParser p, JsonToken token, String msg) {
        super(p, msg);
        this._token = token;
    }
}


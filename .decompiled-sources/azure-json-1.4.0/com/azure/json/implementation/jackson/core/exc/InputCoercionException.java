/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.exc;

import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.JsonToken;
import com.azure.json.implementation.jackson.core.exc.StreamReadException;
import com.azure.json.implementation.jackson.core.util.RequestPayload;

public class InputCoercionException
extends StreamReadException {
    private static final long serialVersionUID = 1L;
    protected final JsonToken _inputType;
    protected final Class<?> _targetType;

    public InputCoercionException(JsonParser p, String msg, JsonToken inputType, Class<?> targetType) {
        super(p, msg);
        this._inputType = inputType;
        this._targetType = targetType;
    }

    @Override
    public InputCoercionException withRequestPayload(RequestPayload p) {
        this._requestPayload = p;
        return this;
    }
}


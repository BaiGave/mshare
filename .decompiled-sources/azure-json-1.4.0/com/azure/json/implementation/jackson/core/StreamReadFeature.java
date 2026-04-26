/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.util.JacksonFeature;

public enum StreamReadFeature implements JacksonFeature
{
    AUTO_CLOSE_SOURCE(JsonParser.Feature.AUTO_CLOSE_SOURCE),
    STRICT_DUPLICATE_DETECTION(JsonParser.Feature.STRICT_DUPLICATE_DETECTION),
    IGNORE_UNDEFINED(JsonParser.Feature.IGNORE_UNDEFINED),
    INCLUDE_SOURCE_IN_LOCATION(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION);

    private final boolean _defaultState;
    private final int _mask;
    private final JsonParser.Feature _mappedFeature;

    private StreamReadFeature(JsonParser.Feature mapTo) {
        this._mappedFeature = mapTo;
        this._mask = mapTo.getMask();
        this._defaultState = mapTo.enabledByDefault();
    }

    @Override
    public boolean enabledByDefault() {
        return this._defaultState;
    }

    @Override
    public boolean enabledIn(int flags) {
        return (flags & this._mask) != 0;
    }

    @Override
    public int getMask() {
        return this._mask;
    }

    public JsonParser.Feature mappedFeature() {
        return this._mappedFeature;
    }
}


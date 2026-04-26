/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.SerializableString;
import com.azure.json.implementation.jackson.core.TSFBuilder;
import com.azure.json.implementation.jackson.core.io.CharacterEscapes;
import com.azure.json.implementation.jackson.core.json.JsonReadFeature;
import com.azure.json.implementation.jackson.core.json.JsonWriteFeature;

public class JsonFactoryBuilder
extends TSFBuilder<JsonFactory, JsonFactoryBuilder> {
    protected CharacterEscapes _characterEscapes;
    protected SerializableString _rootValueSeparator;
    protected int _maximumNonEscapedChar = 0;
    protected char _quoteChar = (char)34;

    public JsonFactoryBuilder() {
        this._rootValueSeparator = JsonFactory.DEFAULT_ROOT_VALUE_SEPARATOR;
    }

    @Override
    public JsonFactoryBuilder enable(JsonReadFeature f) {
        this._legacyEnable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder enable(JsonReadFeature first, JsonReadFeature ... other) {
        this._legacyEnable(first.mappedFeature());
        this.enable(first);
        for (JsonReadFeature f : other) {
            this._legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonReadFeature f) {
        this._legacyDisable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonReadFeature first, JsonReadFeature ... other) {
        this._legacyDisable(first.mappedFeature());
        for (JsonReadFeature f : other) {
            this._legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder configure(JsonReadFeature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    @Override
    public JsonFactoryBuilder enable(JsonWriteFeature f) {
        JsonGenerator.Feature old = f.mappedFeature();
        if (old != null) {
            this._legacyEnable(old);
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder enable(JsonWriteFeature first, JsonWriteFeature ... other) {
        this._legacyEnable(first.mappedFeature());
        for (JsonWriteFeature f : other) {
            this._legacyEnable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonWriteFeature f) {
        this._legacyDisable(f.mappedFeature());
        return this;
    }

    @Override
    public JsonFactoryBuilder disable(JsonWriteFeature first, JsonWriteFeature ... other) {
        this._legacyDisable(first.mappedFeature());
        for (JsonWriteFeature f : other) {
            this._legacyDisable(f.mappedFeature());
        }
        return this;
    }

    @Override
    public JsonFactoryBuilder configure(JsonWriteFeature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    @Override
    public JsonFactory build() {
        return new JsonFactory(this);
    }
}


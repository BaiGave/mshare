/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.StreamReadFeature;
import com.azure.json.implementation.jackson.core.StreamWriteFeature;
import com.azure.json.implementation.jackson.core.io.InputDecorator;
import com.azure.json.implementation.jackson.core.io.OutputDecorator;
import com.azure.json.implementation.jackson.core.json.JsonReadFeature;
import com.azure.json.implementation.jackson.core.json.JsonWriteFeature;

public abstract class TSFBuilder<F extends JsonFactory, B extends TSFBuilder<F, B>> {
    protected static final int DEFAULT_FACTORY_FEATURE_FLAGS = JsonFactory.Feature.collectDefaults();
    protected static final int DEFAULT_PARSER_FEATURE_FLAGS = JsonParser.Feature.collectDefaults();
    protected static final int DEFAULT_GENERATOR_FEATURE_FLAGS = JsonGenerator.Feature.collectDefaults();
    protected int _factoryFeatures = DEFAULT_FACTORY_FEATURE_FLAGS;
    protected int _streamReadFeatures = DEFAULT_PARSER_FEATURE_FLAGS;
    protected int _streamWriteFeatures = DEFAULT_GENERATOR_FEATURE_FLAGS;
    protected InputDecorator _inputDecorator = null;
    protected OutputDecorator _outputDecorator = null;

    protected TSFBuilder() {
    }

    public B enable(JsonFactory.Feature f) {
        this._factoryFeatures |= f.getMask();
        return this._this();
    }

    public B disable(JsonFactory.Feature f) {
        this._factoryFeatures &= ~f.getMask();
        return this._this();
    }

    public B configure(JsonFactory.Feature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    public B enable(StreamReadFeature f) {
        this._streamReadFeatures |= f.mappedFeature().getMask();
        return this._this();
    }

    public B enable(StreamReadFeature first, StreamReadFeature ... other) {
        this._streamReadFeatures |= first.mappedFeature().getMask();
        for (StreamReadFeature f : other) {
            this._streamReadFeatures |= f.mappedFeature().getMask();
        }
        return this._this();
    }

    public B disable(StreamReadFeature f) {
        this._streamReadFeatures &= ~f.mappedFeature().getMask();
        return this._this();
    }

    public B disable(StreamReadFeature first, StreamReadFeature ... other) {
        this._streamReadFeatures &= ~first.mappedFeature().getMask();
        for (StreamReadFeature f : other) {
            this._streamReadFeatures &= ~f.mappedFeature().getMask();
        }
        return this._this();
    }

    public B configure(StreamReadFeature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    public B enable(StreamWriteFeature f) {
        this._streamWriteFeatures |= f.mappedFeature().getMask();
        return this._this();
    }

    public B enable(StreamWriteFeature first, StreamWriteFeature ... other) {
        this._streamWriteFeatures |= first.mappedFeature().getMask();
        for (StreamWriteFeature f : other) {
            this._streamWriteFeatures |= f.mappedFeature().getMask();
        }
        return this._this();
    }

    public B disable(StreamWriteFeature f) {
        this._streamWriteFeatures &= ~f.mappedFeature().getMask();
        return this._this();
    }

    public B disable(StreamWriteFeature first, StreamWriteFeature ... other) {
        this._streamWriteFeatures &= ~first.mappedFeature().getMask();
        for (StreamWriteFeature f : other) {
            this._streamWriteFeatures &= ~f.mappedFeature().getMask();
        }
        return this._this();
    }

    public B configure(StreamWriteFeature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    public B enable(JsonReadFeature f) {
        return this._failNonJSON(f);
    }

    public B enable(JsonReadFeature first, JsonReadFeature ... other) {
        return this._failNonJSON(first);
    }

    public B disable(JsonReadFeature f) {
        return this._failNonJSON(f);
    }

    public B disable(JsonReadFeature first, JsonReadFeature ... other) {
        return this._failNonJSON(first);
    }

    public B configure(JsonReadFeature f, boolean state) {
        return this._failNonJSON(f);
    }

    private B _failNonJSON(Object feature) {
        throw new IllegalArgumentException("Feature " + feature.getClass().getName() + "#" + feature + " not supported for non-JSON backend");
    }

    public B enable(JsonWriteFeature f) {
        return this._failNonJSON(f);
    }

    public B enable(JsonWriteFeature first, JsonWriteFeature ... other) {
        return this._failNonJSON(first);
    }

    public B disable(JsonWriteFeature f) {
        return this._failNonJSON(f);
    }

    public B disable(JsonWriteFeature first, JsonWriteFeature ... other) {
        return this._failNonJSON(first);
    }

    public B configure(JsonWriteFeature f, boolean state) {
        return this._failNonJSON(f);
    }

    public B inputDecorator(InputDecorator dec) {
        this._inputDecorator = dec;
        return this._this();
    }

    public B outputDecorator(OutputDecorator dec) {
        this._outputDecorator = dec;
        return this._this();
    }

    public abstract F build();

    protected final B _this() {
        return (B)this;
    }

    protected void _legacyEnable(JsonParser.Feature f) {
        if (f != null) {
            this._streamReadFeatures |= f.getMask();
        }
    }

    protected void _legacyDisable(JsonParser.Feature f) {
        if (f != null) {
            this._streamReadFeatures &= ~f.getMask();
        }
    }

    protected void _legacyEnable(JsonGenerator.Feature f) {
        if (f != null) {
            this._streamWriteFeatures |= f.getMask();
        }
    }

    protected void _legacyDisable(JsonGenerator.Feature f) {
        if (f != null) {
            this._streamWriteFeatures &= ~f.getMask();
        }
    }
}


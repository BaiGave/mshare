/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriteContext;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.json.JsonWriteFeature;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Objects;

public final class DefaultJsonWriter
extends JsonWriter {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();
    private final JsonGenerator generator;
    private JsonWriteContext context = JsonWriteContext.ROOT;

    public static JsonWriter toStream(OutputStream json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new DefaultJsonWriter(FACTORY.createGenerator(json), options);
    }

    public static JsonWriter toWriter(Writer json, JsonOptions options) throws IOException {
        Objects.requireNonNull(json, "'json' cannot be null.");
        return new DefaultJsonWriter(FACTORY.createGenerator(json), options);
    }

    private DefaultJsonWriter(JsonGenerator generator, JsonOptions options) {
        this.generator = generator;
        this.generator.configure(JsonWriteFeature.WRITE_NAN_AS_STRINGS.mappedFeature(), options.isNonNumericNumbersSupported());
    }

    @Override
    public JsonWriter flush() throws IOException {
        this.generator.flush();
        return this;
    }

    @Override
    public JsonWriter writeStartObject() throws IOException {
        this.context.validateToken(JsonToken.START_OBJECT);
        this.generator.writeStartObject();
        this.context = this.context.updateContext(JsonToken.START_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeEndObject() throws IOException {
        this.context.validateToken(JsonToken.END_OBJECT);
        this.generator.writeEndObject();
        this.context = this.context.updateContext(JsonToken.END_OBJECT);
        return this;
    }

    @Override
    public JsonWriter writeStartArray() throws IOException {
        this.context.validateToken(JsonToken.START_ARRAY);
        this.generator.writeStartArray();
        this.context = this.context.updateContext(JsonToken.START_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeEndArray() throws IOException {
        this.context.validateToken(JsonToken.END_ARRAY);
        this.generator.writeEndArray();
        this.context = this.context.updateContext(JsonToken.END_ARRAY);
        return this;
    }

    @Override
    public JsonWriter writeFieldName(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        this.context.validateToken(JsonToken.FIELD_NAME);
        this.generator.writeFieldName(fieldName);
        this.context = this.context.updateContext(JsonToken.FIELD_NAME);
        return this;
    }

    @Override
    public JsonWriter writeBinary(byte[] value) throws IOException {
        this.context.validateToken(JsonToken.STRING);
        if (value == null) {
            this.generator.writeNull();
        } else {
            this.generator.writeBinary(value);
        }
        this.context = this.context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeBoolean(boolean value) throws IOException {
        this.context.validateToken(JsonToken.BOOLEAN);
        this.generator.writeBoolean(value);
        this.context = this.context.updateContext(JsonToken.BOOLEAN);
        return this;
    }

    @Override
    public JsonWriter writeDouble(double value) throws IOException {
        this.context.validateToken(JsonToken.NUMBER);
        this.generator.writeNumber(value);
        this.context = this.context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeFloat(float value) throws IOException {
        this.context.validateToken(JsonToken.NUMBER);
        this.generator.writeNumber(value);
        this.context = this.context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeInt(int value) throws IOException {
        this.context.validateToken(JsonToken.NUMBER);
        this.generator.writeNumber(value);
        this.context = this.context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeLong(long value) throws IOException {
        this.context.validateToken(JsonToken.NUMBER);
        this.generator.writeNumber(value);
        this.context = this.context.updateContext(JsonToken.NUMBER);
        return this;
    }

    @Override
    public JsonWriter writeNull() throws IOException {
        this.context.validateToken(JsonToken.NULL);
        this.generator.writeNull();
        this.context = this.context.updateContext(JsonToken.NULL);
        return this;
    }

    @Override
    public JsonWriter writeString(String value) throws IOException {
        this.context.validateToken(JsonToken.STRING);
        this.generator.writeString(value);
        this.context = this.context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriter writeRawValue(String value) throws IOException {
        Objects.requireNonNull(value, "'value' cannot be null.");
        this.context.validateToken(JsonToken.STRING);
        this.generator.writeRawValue(value);
        this.context = this.context.updateContext(JsonToken.STRING);
        return this;
    }

    @Override
    public JsonWriteContext getWriteContext() {
        return this.context;
    }

    @Override
    public void close() throws IOException {
        if (this.context != JsonWriteContext.COMPLETED) {
            throw new IllegalStateException("Writing of the JSON object must be completed before the writer can be closed. Current writing state is '" + (Object)((Object)this.context.getWriteState()) + "'.");
        }
        this.generator.flush();
        this.generator.close();
    }
}


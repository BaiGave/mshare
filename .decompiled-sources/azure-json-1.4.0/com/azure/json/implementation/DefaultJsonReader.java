/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.json.JsonReadFeature;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public final class DefaultJsonReader
extends JsonReader {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();
    private final JsonParser parser;
    private final byte[] jsonBytes;
    private final String jsonString;
    private final boolean resetSupported;
    private final boolean nonNumericNumbersSupported;
    private final boolean jsoncSupported;
    private JsonToken currentToken;

    public static JsonReader fromBytes(byte[] json, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), true, json, null, options);
    }

    public static JsonReader fromString(String json, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), true, null, json, options);
    }

    public static JsonReader fromStream(InputStream json, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(json), json.markSupported(), null, null, options);
    }

    public static JsonReader fromReader(Reader reader, JsonOptions options) throws IOException {
        return new DefaultJsonReader(FACTORY.createParser(reader), reader.markSupported(), null, null, options);
    }

    private DefaultJsonReader(JsonParser parser, boolean resetSupported, byte[] jsonBytes, String jsonString, JsonOptions options) {
        this(parser, resetSupported, jsonBytes, jsonString, options.isNonNumericNumbersSupported(), options.isJsoncSupported());
    }

    private DefaultJsonReader(JsonParser parser, boolean resetSupported, byte[] jsonBytes, String jsonString, boolean nonNumericNumbersSupported, boolean jsoncSupported) {
        this.parser = parser;
        this.resetSupported = resetSupported;
        this.parser.configure(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature(), nonNumericNumbersSupported);
        this.parser.configure(JsonParser.Feature.ALLOW_COMMENTS, jsoncSupported);
        this.jsonBytes = jsonBytes;
        this.jsonString = jsonString;
        this.nonNumericNumbersSupported = nonNumericNumbersSupported;
        this.jsoncSupported = jsoncSupported;
    }

    @Override
    public JsonToken currentToken() {
        return this.currentToken;
    }

    @Override
    public JsonToken nextToken() throws IOException {
        this.currentToken = DefaultJsonReader.mapToken(this.parser.nextToken(), this.currentToken);
        return this.currentToken;
    }

    @Override
    public byte[] getBinary() throws IOException {
        if (this.currentToken() == JsonToken.NULL) {
            return null;
        }
        return this.parser.getBinaryValue();
    }

    @Override
    public boolean getBoolean() throws IOException {
        return this.parser.getBooleanValue();
    }

    @Override
    public double getDouble() throws IOException {
        return this.parser.getDoubleValue();
    }

    @Override
    public float getFloat() throws IOException {
        return this.parser.getFloatValue();
    }

    @Override
    public int getInt() throws IOException {
        return this.parser.getIntValue();
    }

    @Override
    public long getLong() throws IOException {
        return this.parser.getLongValue();
    }

    @Override
    public String getString() throws IOException {
        return this.parser.getValueAsString();
    }

    @Override
    public String getFieldName() throws IOException {
        return this.parser.currentName();
    }

    @Override
    public void skipChildren() throws IOException {
        this.parser.skipChildren();
    }

    @Override
    public JsonReader bufferObject() throws IOException {
        JsonToken currentToken = this.currentToken();
        if (currentToken == JsonToken.START_OBJECT || currentToken == JsonToken.FIELD_NAME) {
            String json = this.readRemainingFieldsAsJsonObject();
            return new DefaultJsonReader(FACTORY.createParser(json), true, null, json, this.nonNumericNumbersSupported, this.jsoncSupported);
        }
        throw new IllegalStateException("Cannot buffer a JSON object from a non-object, non-field name starting location. Starting location: " + (Object)((Object)this.currentToken()));
    }

    @Override
    public boolean isResetSupported() {
        return this.resetSupported;
    }

    @Override
    public JsonReader reset() throws IOException {
        if (!this.resetSupported) {
            throw new IllegalStateException("'reset' isn't supported by this JsonReader.");
        }
        if (this.jsonBytes != null) {
            return new DefaultJsonReader(FACTORY.createParser(this.jsonBytes), true, this.jsonBytes, null, this.nonNumericNumbersSupported, this.jsoncSupported);
        }
        return new DefaultJsonReader(FACTORY.createParser(this.jsonString), true, null, this.jsonString, this.nonNumericNumbersSupported, this.jsoncSupported);
    }

    @Override
    public void close() throws IOException {
        this.parser.close();
    }

    private static JsonToken mapToken(com.azure.json.implementation.jackson.core.JsonToken nextToken, JsonToken currentToken) {
        if (nextToken == null && currentToken == null) {
            return null;
        }
        if (nextToken == null) {
            return JsonToken.END_DOCUMENT;
        }
        switch (nextToken) {
            case START_OBJECT: {
                return JsonToken.START_OBJECT;
            }
            case END_OBJECT: {
                return JsonToken.END_OBJECT;
            }
            case START_ARRAY: {
                return JsonToken.START_ARRAY;
            }
            case END_ARRAY: {
                return JsonToken.END_ARRAY;
            }
            case FIELD_NAME: {
                return JsonToken.FIELD_NAME;
            }
            case VALUE_STRING: {
                return JsonToken.STRING;
            }
            case VALUE_NUMBER_INT: 
            case VALUE_NUMBER_FLOAT: {
                return JsonToken.NUMBER;
            }
            case VALUE_TRUE: 
            case VALUE_FALSE: {
                return JsonToken.BOOLEAN;
            }
            case VALUE_NULL: {
                return JsonToken.NULL;
            }
        }
        throw new IllegalStateException("Unsupported token type: '" + (Object)((Object)nextToken) + "'.");
    }
}


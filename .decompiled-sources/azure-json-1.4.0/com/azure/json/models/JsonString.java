/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.jackson.core.io.JsonStringEncoder;
import com.azure.json.models.JsonElement;
import java.io.IOException;

public final class JsonString
extends JsonElement {
    private final String value;
    private String jsonString;

    public JsonString(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeString(this.value);
    }

    public static JsonString fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }
        if (token != JsonToken.STRING) {
            throw new IllegalStateException("JsonReader is pointing to an invalid token for deserialization. Token was: " + (Object)((Object)token) + ".");
        }
        return new JsonString(jsonReader.getString());
    }

    @Override
    public String toJsonString() throws IOException {
        if (this.jsonString != null) {
            return this.jsonString;
        }
        StringBuilder sb = new StringBuilder(this.value.length() + 32);
        sb.append('\"');
        JsonStringEncoder.getInstance().quoteAsString(this.value, sb);
        sb.append('\"');
        this.jsonString = sb.toString();
        return this.jsonString;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.models.JsonElement;
import java.io.IOException;

public final class JsonBoolean
extends JsonElement {
    private static final JsonBoolean TRUE = new JsonBoolean(true);
    private static final JsonBoolean FALSE = new JsonBoolean(false);
    private final boolean value;

    private JsonBoolean(boolean value) {
        this.value = value;
    }

    public static JsonBoolean getInstance(boolean value) {
        return value ? TRUE : FALSE;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeBoolean(this.value);
    }

    public static JsonBoolean fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }
        if (token != JsonToken.BOOLEAN) {
            throw new IllegalStateException("JsonReader is pointing to an invalid token for deserialization. Token was: " + (Object)((Object)token) + ".");
        }
        return JsonBoolean.getInstance(jsonReader.getBoolean());
    }

    @Override
    public String toJsonString() throws IOException {
        return Boolean.toString(this.value);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.models.JsonElement;
import java.io.IOException;

public final class JsonNull
extends JsonElement {
    private static final JsonNull INSTANCE = new JsonNull();

    private JsonNull() {
    }

    public static JsonNull getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeNull();
    }

    public static JsonNull fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }
        if (token != JsonToken.NULL) {
            throw new IllegalStateException("JsonReader is pointing to an invalid token for deserialization. Token was: " + (Object)((Object)token) + ".");
        }
        return JsonNull.getInstance();
    }

    @Override
    public String toJsonString() throws IOException {
        return "null";
    }
}


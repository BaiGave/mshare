/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.StringBuilderWriter;
import com.azure.json.models.JsonBoolean;
import com.azure.json.models.JsonElement;
import com.azure.json.models.JsonNull;
import com.azure.json.models.JsonNumber;
import com.azure.json.models.JsonString;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class JsonObject
extends JsonElement {
    private final Map<String, JsonElement> properties;

    public JsonObject() {
        this.properties = new LinkedHashMap<String, JsonElement>();
    }

    private JsonObject(Map<String, JsonElement> properties) {
        this.properties = properties;
    }

    public boolean hasProperty(String key) {
        return this.properties.containsKey(key);
    }

    public JsonElement getProperty(String key) {
        return this.properties.get(key);
    }

    public JsonObject setProperty(String key, JsonElement element) {
        this.properties.put(key, JsonObject.nullCheck(element));
        return this;
    }

    public JsonObject setProperty(String key, boolean element) {
        this.properties.put(key, JsonBoolean.getInstance(element));
        return this;
    }

    public JsonObject setProperty(String key, Number element) {
        this.properties.put(key, element == null ? JsonNull.getInstance() : new JsonNumber(element));
        return this;
    }

    public JsonObject setProperty(String key, String element) {
        this.properties.put(key, element == null ? JsonNull.getInstance() : new JsonString(element));
        return this;
    }

    public JsonElement removeProperty(String key) {
        return this.properties.remove(key);
    }

    public int size() {
        return this.properties.size();
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeMap(this.properties, JsonWriter::writeJson);
    }

    public static JsonObject fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }
        if (token != JsonToken.START_OBJECT) {
            throw new IllegalStateException("JsonReader is pointing to an invalid token for deserialization. Token was: " + (Object)((Object)token) + ".");
        }
        return new JsonObject(jsonReader.readMap(JsonElement::fromJson));
    }

    @Override
    public String toJsonString() throws IOException {
        StringBuilderWriter writer = new StringBuilderWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer);){
            this.toJson(jsonWriter).flush();
            String string = writer.toString();
            return string;
        }
    }

    private static JsonElement nullCheck(JsonElement element) {
        return Objects.requireNonNull(element, "The JsonElement cannot be null. If null must be represented in JSON, use JsonNull.");
    }
}


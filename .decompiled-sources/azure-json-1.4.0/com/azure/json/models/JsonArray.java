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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class JsonArray
extends JsonElement {
    private final List<JsonElement> elements;

    public JsonArray() {
        this.elements = new LinkedList<JsonElement>();
    }

    private JsonArray(List<JsonElement> elements) {
        this.elements = elements;
    }

    public JsonArray addElement(JsonElement element) {
        this.elements.add(JsonArray.nullCheck(element));
        return this;
    }

    public JsonArray addElement(boolean element) {
        this.elements.add(JsonBoolean.getInstance(element));
        return this;
    }

    public JsonArray addElement(Number element) {
        this.elements.add(element == null ? JsonNull.getInstance() : new JsonNumber(element));
        return this;
    }

    public JsonArray addElement(String element) {
        this.elements.add(element == null ? JsonNull.getInstance() : new JsonString(element));
        return this;
    }

    public JsonArray addElement(int index, JsonElement element) {
        this.elements.add(index, JsonArray.nullCheck(element));
        return this;
    }

    public JsonArray addElement(int index, boolean element) {
        this.elements.add(index, JsonBoolean.getInstance(element));
        return this;
    }

    public JsonArray addElement(int index, Number element) {
        this.elements.add(index, element == null ? JsonNull.getInstance() : new JsonNumber(element));
        return this;
    }

    public JsonArray addElement(int index, String element) {
        this.elements.add(index, element == null ? JsonNull.getInstance() : new JsonString(element));
        return this;
    }

    public JsonArray setElement(int index, JsonElement element) {
        this.elements.set(index, JsonArray.nullCheck(element));
        return this;
    }

    public JsonArray setElement(int index, boolean element) {
        this.elements.set(index, JsonBoolean.getInstance(element));
        return this;
    }

    public JsonArray setElement(int index, Number element) {
        this.elements.set(index, element == null ? JsonNull.getInstance() : new JsonNumber(element));
        return this;
    }

    public JsonArray setElement(int index, String element) {
        this.elements.set(index, element == null ? JsonNull.getInstance() : new JsonString(element));
        return this;
    }

    public JsonElement getElement(int index) throws IndexOutOfBoundsException {
        return this.elements.get(index);
    }

    public JsonElement removeElement(int index) throws IndexOutOfBoundsException {
        return this.elements.remove(index);
    }

    public int size() {
        return this.elements.size();
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeArray(this.elements, JsonWriter::writeJson);
    }

    public static JsonArray fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }
        if (token != JsonToken.START_ARRAY) {
            throw new IllegalStateException("JsonReader is pointing to an invalid token for deserialization. Token was: " + (Object)((Object)token) + ".");
        }
        return new JsonArray(jsonReader.readArray(JsonElement::fromJson));
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


/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.models.JsonArray;
import com.azure.json.models.JsonBoolean;
import com.azure.json.models.JsonNull;
import com.azure.json.models.JsonNumber;
import com.azure.json.models.JsonObject;
import com.azure.json.models.JsonString;
import java.io.IOException;

public abstract class JsonElement
implements JsonSerializable<JsonElement> {
    static JsonElement fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }
        switch (token) {
            case START_OBJECT: {
                return JsonObject.fromJson(jsonReader);
            }
            case START_ARRAY: {
                return JsonArray.fromJson(jsonReader);
            }
            case STRING: {
                return new JsonString(jsonReader.getString());
            }
            case NUMBER: {
                return new JsonNumber(jsonReader.getString());
            }
            case BOOLEAN: {
                return JsonBoolean.getInstance(jsonReader.getBoolean());
            }
            case NULL: {
                return JsonNull.getInstance();
            }
        }
        throw new IllegalStateException("JsonReader is pointing to an invalid token for deserialization. Token was: " + (Object)((Object)token) + ".");
    }

    public boolean isArray() {
        return false;
    }

    public JsonArray asArray() {
        return (JsonArray)this;
    }

    public boolean isObject() {
        return false;
    }

    public JsonObject asObject() {
        return (JsonObject)this;
    }

    public boolean isBoolean() {
        return false;
    }

    public JsonBoolean asBoolean() {
        return (JsonBoolean)this;
    }

    public boolean isNull() {
        return false;
    }

    public JsonNull asNull() {
        return (JsonNull)this;
    }

    public boolean isNumber() {
        return false;
    }

    public JsonNumber asNumber() {
        return (JsonNumber)this;
    }

    public boolean isString() {
        return false;
    }

    public JsonString asString() {
        return (JsonString)this;
    }
}


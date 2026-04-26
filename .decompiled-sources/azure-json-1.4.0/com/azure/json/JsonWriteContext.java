/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

import com.azure.json.JsonToken;
import com.azure.json.JsonWriteState;

public final class JsonWriteContext {
    public static final JsonWriteContext ROOT = new JsonWriteContext(null, JsonWriteState.ROOT);
    public static final JsonWriteContext COMPLETED = new JsonWriteContext(null, JsonWriteState.COMPLETED);
    private final JsonWriteContext parent;
    private final JsonWriteState context;

    private JsonWriteContext(JsonWriteContext parent, JsonWriteState context) {
        this.parent = parent;
        this.context = context;
    }

    public JsonWriteContext getParent() {
        return this.parent;
    }

    public JsonWriteState getWriteState() {
        return this.context;
    }

    public void validateToken(JsonToken token) {
        if (this.context == JsonWriteState.ROOT) {
            if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY || token == JsonToken.FIELD_NAME) {
                throw new IllegalStateException("Writing context is 'ROOT', only 'START_OBJECT', 'START_ARRAY', 'BOOLEAN', 'NULL', 'NUMBER', or 'STRING' tokens are allowed. Attempted: '" + (Object)((Object)token) + "'.");
            }
        } else if (this.context == JsonWriteState.OBJECT) {
            if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY || token == JsonToken.END_ARRAY || JsonWriteContext.isSimpleValue(token)) {
                throw new IllegalStateException("Writing context is 'OBJECT', only 'END_OBJECT' and 'FIELD_NAME' tokens are allowed. Attempted: '" + (Object)((Object)token) + "'.");
            }
        } else if (this.context == JsonWriteState.ARRAY) {
            if (token == JsonToken.END_OBJECT || token == JsonToken.FIELD_NAME) {
                throw new IllegalStateException("Writing context is 'ARRAY', only 'START_OBJECT', 'START_ARRAY',, 'END_ARRAY', 'BOOLEAN', 'NULL', 'NUMBER', or 'STRING' tokens are allowed. Attempted: '" + (Object)((Object)token) + "'.");
            }
        } else if (this.context == JsonWriteState.FIELD) {
            if (token == JsonToken.END_OBJECT || token == JsonToken.END_ARRAY || token == JsonToken.FIELD_NAME) {
                throw new IllegalStateException("Writing context is 'FIELD', only 'START_OBJECT', 'START_ARRAY', 'BOOLEAN', 'NULL', 'NUMBER', or 'STRING' tokens are allowed. Attempted: '" + (Object)((Object)token) + "'.");
            }
        } else {
            throw new IllegalStateException("Writing context is 'COMPLETED', no further tokens are allowed. Attempted: '" + (Object)((Object)token) + "'.");
        }
    }

    public JsonWriteContext updateContext(JsonToken token) {
        if (JsonWriteContext.isSimpleValue(token)) {
            if (this.context == JsonWriteState.ROOT) {
                return COMPLETED;
            }
            if (this.context == JsonWriteState.ARRAY) {
                return this;
            }
            return this.parent;
        }
        if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) {
            JsonWriteContext toReturn = this.parent;
            if (toReturn.context == JsonWriteState.ROOT) {
                return COMPLETED;
            }
            if (toReturn.context == JsonWriteState.FIELD) {
                return toReturn.parent;
            }
            return toReturn;
        }
        if (token == JsonToken.START_OBJECT) {
            return new JsonWriteContext(this, JsonWriteState.OBJECT);
        }
        if (token == JsonToken.START_ARRAY) {
            return new JsonWriteContext(this, JsonWriteState.ARRAY);
        }
        if (token == JsonToken.FIELD_NAME) {
            return new JsonWriteContext(this, JsonWriteState.FIELD);
        }
        return this;
    }

    private static boolean isSimpleValue(JsonToken token) {
        return token == JsonToken.BOOLEAN || token == JsonToken.NULL || token == JsonToken.NUMBER || token == JsonToken.STRING;
    }
}


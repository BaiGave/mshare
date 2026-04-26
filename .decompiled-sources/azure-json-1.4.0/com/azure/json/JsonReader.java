/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

import com.azure.json.JsonToken;
import com.azure.json.ReadValueCallback;
import com.azure.json.implementation.jackson.core.io.JsonStringEncoder;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class JsonReader
implements Closeable {
    private static final JsonStringEncoder ENCODER = JsonStringEncoder.getInstance();

    public abstract JsonToken currentToken();

    public abstract JsonToken nextToken() throws IOException;

    @Override
    public abstract void close() throws IOException;

    public final boolean isStartArrayOrObject() {
        return JsonReader.isStartArrayOrObject(this.currentToken());
    }

    private static boolean isStartArrayOrObject(JsonToken token) {
        return token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT;
    }

    public final boolean isEndArrayOrObject() {
        return JsonReader.isEndArrayOrObject(this.currentToken());
    }

    private static boolean isEndArrayOrObject(JsonToken token) {
        return token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT;
    }

    public abstract byte[] getBinary() throws IOException;

    public abstract boolean getBoolean() throws IOException;

    public abstract float getFloat() throws IOException;

    public abstract double getDouble() throws IOException;

    public abstract int getInt() throws IOException;

    public abstract long getLong() throws IOException;

    public abstract String getString() throws IOException;

    public abstract String getFieldName() throws IOException;

    public final <T> T getNullable(ReadValueCallback<JsonReader, T> nonNullGetter) throws IOException {
        return this.currentToken() == JsonToken.NULL ? null : (T)nonNullGetter.read(this);
    }

    public abstract void skipChildren() throws IOException;

    public abstract JsonReader bufferObject() throws IOException;

    public abstract boolean isResetSupported();

    public abstract JsonReader reset() throws IOException;

    public final String readChildren() throws IOException {
        return this.readInternal(new StringBuilder(), true, false).toString();
    }

    public final void readChildren(StringBuilder buffer) throws IOException {
        this.readInternal(buffer, true, false);
    }

    public final String readRemainingFieldsAsJsonObject() throws IOException {
        return this.readInternal(new StringBuilder(), false, true).toString();
    }

    public final void readRemainingFieldsAsJsonObject(StringBuilder buffer) throws IOException {
        this.readInternal(buffer, false, true);
    }

    private StringBuilder readInternal(StringBuilder buffer, boolean canStartAtArray, boolean canStartAtFieldName) throws IOException {
        boolean canRead;
        Objects.requireNonNull(buffer, "The 'buffer' used to read the JSON object cannot be null.");
        JsonToken token = this.currentToken();
        boolean bl = canRead = token == JsonToken.START_OBJECT || canStartAtArray && token == JsonToken.START_ARRAY || canStartAtFieldName && token == JsonToken.FIELD_NAME;
        if (!canRead) {
            return buffer;
        }
        if (token == JsonToken.FIELD_NAME) {
            buffer.append("{\"");
            ENCODER.quoteAsString(this.getFieldName(), buffer);
            buffer.append("\":");
            token = this.nextToken();
        }
        this.appendJson(buffer, token);
        int depth = 1;
        while (depth > 0) {
            JsonToken previousToken = token;
            token = this.nextToken();
            if (JsonReader.isStartArrayOrObject(token)) {
                ++depth;
            } else if (JsonReader.isEndArrayOrObject(token)) {
                --depth;
            } else if (token == null) {
                return buffer;
            }
            if (!JsonReader.isStartArrayOrObject(previousToken) && !JsonReader.isEndArrayOrObject(token) && previousToken != JsonToken.FIELD_NAME) {
                buffer.append(',');
            }
            this.appendJson(buffer, token);
        }
        return buffer;
    }

    private void appendJson(StringBuilder buffer, JsonToken token) throws IOException {
        if (token == JsonToken.FIELD_NAME) {
            buffer.append("\"");
            ENCODER.quoteAsString(this.getFieldName(), buffer);
            buffer.append("\":");
        } else if (token == JsonToken.STRING) {
            buffer.append("\"");
            ENCODER.quoteAsString(this.getString(), buffer);
            buffer.append("\"");
        } else {
            buffer.append(this.getText());
        }
    }

    public final <T> T readObject(ReadValueCallback<JsonReader, T> objectReaderFunc) throws IOException {
        return this.readMapOrObject(objectReaderFunc, false);
    }

    public final <T> List<T> readArray(ReadValueCallback<JsonReader, T> elementReaderFunc) throws IOException {
        JsonToken currentToken = this.currentToken();
        if (currentToken == null) {
            currentToken = this.nextToken();
        }
        if (currentToken == JsonToken.NULL || currentToken == null) {
            return null;
        }
        if (currentToken != JsonToken.START_ARRAY) {
            throw new IllegalStateException("Unexpected token to begin array deserialization: " + (Object)((Object)currentToken));
        }
        LinkedList<T> array = new LinkedList<T>();
        while (this.nextToken() != JsonToken.END_ARRAY) {
            array.add(elementReaderFunc.read(this));
        }
        return array;
    }

    public final <T> Map<String, T> readMap(ReadValueCallback<JsonReader, T> valueReaderFunc) throws IOException {
        return this.readMapOrObject(reader -> {
            LinkedHashMap map = new LinkedHashMap();
            while (this.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = this.getFieldName();
                this.nextToken();
                map.put(fieldName, valueReaderFunc.read(this));
            }
            return map;
        }, true);
    }

    private <T> T readMapOrObject(ReadValueCallback<JsonReader, T> valueReaderFunc, boolean isMap) throws IOException {
        JsonToken currentToken = this.currentToken();
        if (currentToken == null) {
            currentToken = this.nextToken();
        }
        if (currentToken == JsonToken.NULL || currentToken == null) {
            return null;
        }
        if (currentToken != JsonToken.START_OBJECT) {
            String type = isMap ? "map" : "object";
            throw new IllegalStateException("Unexpected token to begin " + type + " deserialization: " + (Object)((Object)currentToken));
        }
        return valueReaderFunc.read(this);
    }

    public final Object readUntyped() throws IOException {
        JsonToken token = this.currentToken();
        if (token == null) {
            token = this.nextToken();
        }
        if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT || token == JsonToken.FIELD_NAME) {
            throw new IllegalStateException("Unexpected token to begin an untyped field: " + (Object)((Object)token));
        }
        return this.readUntypedHelper(0);
    }

    private Object readUntypedHelper(int depth) throws IOException {
        if (depth >= 999) {
            throw new IllegalStateException("Untyped object exceeded allowed object nested depth of 1000.");
        }
        JsonToken token = this.currentToken();
        if (token == JsonToken.NULL || token == null) {
            return null;
        }
        if (token == JsonToken.BOOLEAN) {
            return this.getBoolean();
        }
        if (token == JsonToken.NUMBER) {
            String numberText = this.getText();
            if ("INF".equals(numberText) || "Infinity".equals(numberText) || "-INF".equals(numberText) || "-Infinity".equals(numberText) || "NaN".equals(numberText)) {
                return numberText;
            }
            if (numberText.contains(".")) {
                return Double.parseDouble(numberText);
            }
            try {
                return Integer.parseInt(numberText);
            }
            catch (NumberFormatException ex) {
                return Long.parseLong(numberText);
            }
        }
        if (token == JsonToken.STRING) {
            return this.getString();
        }
        if (token == JsonToken.START_ARRAY) {
            ArrayList<Object> array = new ArrayList<Object>();
            while (this.nextToken() != JsonToken.END_ARRAY) {
                array.add(this.readUntypedHelper(depth + 1));
            }
            return array;
        }
        if (token == JsonToken.START_OBJECT) {
            LinkedHashMap<String, Object> object = new LinkedHashMap<String, Object>();
            while (this.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = this.getFieldName();
                this.nextToken();
                Object value = this.readUntypedHelper(depth + 1);
                object.put(fieldName, value);
            }
            return object;
        }
        throw new IllegalStateException("Unknown token type while reading an untyped field: " + (Object)((Object)token));
    }

    public final String getText() throws IOException {
        return this.getTextInternal(false);
    }

    public String getRawText() throws IOException {
        return this.getTextInternal(true);
    }

    private String getTextInternal(boolean raw) throws IOException {
        JsonToken token = this.currentToken();
        if (token == null) {
            throw new IllegalStateException("Current token cannot be null.");
        }
        switch (token) {
            case START_OBJECT: {
                return "{";
            }
            case END_OBJECT: {
                return "}";
            }
            case START_ARRAY: {
                return "[";
            }
            case END_ARRAY: {
                return "]";
            }
            case FIELD_NAME: {
                return raw ? new String(ENCODER.quoteAsUTF8(this.getFieldName()), StandardCharsets.UTF_8) : this.getFieldName();
            }
            case BOOLEAN: {
                return String.valueOf(this.getBoolean());
            }
            case NUMBER: {
                return this.getString();
            }
            case STRING: {
                return raw ? new String(ENCODER.quoteAsUTF8(this.getString()), StandardCharsets.UTF_8) : this.getString();
            }
            case NULL: {
                return "null";
            }
        }
        return "";
    }
}


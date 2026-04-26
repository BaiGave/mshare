/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriteContext;
import com.azure.json.WriteValueCallback;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public abstract class JsonWriter
implements Closeable {
    public abstract JsonWriteContext getWriteContext();

    @Override
    public abstract void close() throws IOException;

    public abstract JsonWriter flush() throws IOException;

    public abstract JsonWriter writeStartObject() throws IOException;

    public final JsonWriter writeStartObject(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeStartObject();
    }

    public abstract JsonWriter writeEndObject() throws IOException;

    public abstract JsonWriter writeStartArray() throws IOException;

    public final JsonWriter writeStartArray(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeStartArray();
    }

    public abstract JsonWriter writeEndArray() throws IOException;

    public abstract JsonWriter writeFieldName(String var1) throws IOException;

    public final JsonWriter writeJson(JsonSerializable<?> value) throws IOException {
        return value == null ? this : value.toJson(this);
    }

    public final <T> JsonWriter writeArray(T[] array, WriteValueCallback<JsonWriter, T> elementWriterFunc) throws IOException {
        return this.writeArray(array, elementWriterFunc, false);
    }

    public <T> JsonWriter writeArray(T[] array, WriteValueCallback<JsonWriter, T> elementWriterFunc, boolean skipNullElements) throws IOException {
        Objects.requireNonNull(elementWriterFunc, "'elementWriterFunc' cannot be null.");
        if (array == null) {
            return this.writeNull();
        }
        return this.writeArrayInternal(Arrays.asList(array), elementWriterFunc, null, skipNullElements);
    }

    public final <T> JsonWriter writeArray(Iterable<T> array, WriteValueCallback<JsonWriter, T> elementWriterFunc) throws IOException {
        return this.writeArray(array, elementWriterFunc, false);
    }

    public <T> JsonWriter writeArray(Iterable<T> array, WriteValueCallback<JsonWriter, T> elementWriterFunc, boolean skipNullElements) throws IOException {
        Objects.requireNonNull(elementWriterFunc, "'elementWriterFunc' cannot be null.");
        if (array == null) {
            return this.writeNull();
        }
        return this.writeArrayInternal(array, elementWriterFunc, null, skipNullElements);
    }

    private <T> JsonWriter writeArrayInternal(Iterable<T> array, WriteValueCallback<JsonWriter, T> func, String fieldName, boolean skipNullElements) throws IOException {
        if (fieldName == null) {
            this.writeStartArray();
        } else {
            this.writeStartArray(fieldName);
        }
        for (T element : array) {
            if (element == null) {
                if (skipNullElements) continue;
                this.writeNull();
                continue;
            }
            func.write(this, (JsonWriter)element);
        }
        return this.writeEndArray();
    }

    public final <T> JsonWriter writeMap(Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc) throws IOException {
        return this.writeMap(map, valueWriterFunc, false);
    }

    public <T> JsonWriter writeMap(Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc, boolean skipNullValues) throws IOException {
        Objects.requireNonNull(valueWriterFunc, "'valueWriterFunc' cannot be null.");
        if (map == null) {
            return this.writeNull();
        }
        return this.writeMapInternal(null, map, valueWriterFunc, skipNullValues);
    }

    private <T> JsonWriter writeMapInternal(String fieldName, Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc, boolean skipNullValues) throws IOException {
        if (fieldName == null) {
            this.writeStartObject();
        } else {
            this.writeStartObject(fieldName);
        }
        for (Map.Entry<String, T> entry : map.entrySet()) {
            T value = entry.getValue();
            if (skipNullValues && value == null) continue;
            this.writeFieldName(entry.getKey());
            if (value == null) {
                this.writeNull();
                continue;
            }
            valueWriterFunc.write(this, (JsonWriter)value);
        }
        return this.writeEndObject();
    }

    public abstract JsonWriter writeBinary(byte[] var1) throws IOException;

    public abstract JsonWriter writeBoolean(boolean var1) throws IOException;

    public final JsonWriter writeBoolean(Boolean value) throws IOException {
        return value == null ? this.writeNull() : this.writeBoolean((boolean)value);
    }

    public abstract JsonWriter writeDouble(double var1) throws IOException;

    public abstract JsonWriter writeFloat(float var1) throws IOException;

    public abstract JsonWriter writeInt(int var1) throws IOException;

    public abstract JsonWriter writeLong(long var1) throws IOException;

    public abstract JsonWriter writeNull() throws IOException;

    public final JsonWriter writeNumber(Number value) throws IOException {
        if (value == null) {
            return this.writeNull();
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
            return this.writeInt(value.intValue());
        }
        if (value instanceof Long) {
            return this.writeLong(value.longValue());
        }
        if (value instanceof Float) {
            return this.writeFloat(value.floatValue());
        }
        if (value instanceof Double) {
            return this.writeDouble(value.doubleValue());
        }
        return this.writeRawValue(value.toString());
    }

    public abstract JsonWriter writeString(String var1) throws IOException;

    public abstract JsonWriter writeRawValue(String var1) throws IOException;

    public final <T> JsonWriter writeNullableField(String fieldName, T nullable, WriteValueCallback<JsonWriter, T> writerFunc) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        Objects.requireNonNull(writerFunc, "'writerFunc' cannot be null.");
        if (nullable == null) {
            return this.writeNullField(fieldName);
        }
        writerFunc.write(this.writeFieldName(fieldName), (JsonWriter)nullable);
        return this;
    }

    public final JsonWriter writeJsonField(String fieldName, JsonSerializable<?> value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return value == null ? this : value.toJson(this.writeFieldName(fieldName));
    }

    public final <T> JsonWriter writeArrayField(String fieldName, T[] array, WriteValueCallback<JsonWriter, T> elementWriterFunc) throws IOException {
        return this.writeArrayField(fieldName, array, elementWriterFunc, false);
    }

    public <T> JsonWriter writeArrayField(String fieldName, T[] array, WriteValueCallback<JsonWriter, T> elementWriterFunc, boolean skipNullElements) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        Objects.requireNonNull(elementWriterFunc, "'elementWriterFunc' cannot be null.");
        if (array == null) {
            return this;
        }
        return this.writeArrayInternal(Arrays.asList(array), elementWriterFunc, fieldName, skipNullElements);
    }

    public final <T> JsonWriter writeArrayField(String fieldName, Iterable<T> array, WriteValueCallback<JsonWriter, T> elementWriterFunc) throws IOException {
        return this.writeArrayField(fieldName, array, elementWriterFunc, false);
    }

    public <T> JsonWriter writeArrayField(String fieldName, Iterable<T> array, WriteValueCallback<JsonWriter, T> elementWriterFunc, boolean skipNullElements) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        Objects.requireNonNull(elementWriterFunc, "'elementWriterFunc' cannot be null.");
        if (array == null) {
            return this;
        }
        return this.writeArrayInternal(array, elementWriterFunc, fieldName, skipNullElements);
    }

    public final <T> JsonWriter writeMapField(String fieldName, Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc) throws IOException {
        return this.writeMapField(fieldName, map, valueWriterFunc, false);
    }

    public <T> JsonWriter writeMapField(String fieldName, Map<String, T> map, WriteValueCallback<JsonWriter, T> valueWriterFunc, boolean skipNullValues) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        Objects.requireNonNull(valueWriterFunc, "'valueWriterFunc' cannot be null.");
        if (map == null) {
            return this;
        }
        return this.writeMapInternal(fieldName, map, valueWriterFunc, skipNullValues);
    }

    public final JsonWriter writeBinaryField(String fieldName, byte[] value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        if (value == null) {
            return this;
        }
        return this.writeFieldName(fieldName).writeBinary(value);
    }

    public final JsonWriter writeBooleanField(String fieldName, boolean value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeBoolean(value);
    }

    public final JsonWriter writeBooleanField(String fieldName, Boolean value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return value == null ? this : this.writeBooleanField(fieldName, (boolean)value);
    }

    public final JsonWriter writeDoubleField(String fieldName, double value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeDouble(value);
    }

    public final JsonWriter writeFloatField(String fieldName, float value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeFloat(value);
    }

    public final JsonWriter writeIntField(String fieldName, int value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeInt(value);
    }

    public final JsonWriter writeLongField(String fieldName, long value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeLong(value);
    }

    public final JsonWriter writeNullField(String fieldName) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeNull();
    }

    public final JsonWriter writeNumberField(String fieldName, Number value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        if (value == null) {
            return this;
        }
        return this.writeFieldName(fieldName).writeNumber(value);
    }

    public final JsonWriter writeStringField(String fieldName, String value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        if (value == null) {
            return this;
        }
        return this.writeFieldName(fieldName).writeString(value);
    }

    public final JsonWriter writeRawField(String fieldName, String value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        Objects.requireNonNull(value, "'value' cannot be null.");
        return this.writeFieldName(fieldName).writeRawValue(value);
    }

    public JsonWriter writeUntypedField(String fieldName, Object value) throws IOException {
        Objects.requireNonNull(fieldName, "'fieldName' cannot be null.");
        return this.writeFieldName(fieldName).writeUntyped(value);
    }

    public JsonWriter writeUntyped(Object value) throws IOException {
        if (value == null) {
            return this.writeNull();
        }
        if (value instanceof Short) {
            return this.writeInt(((Short)value).shortValue());
        }
        if (value instanceof Integer) {
            return this.writeInt((Integer)value);
        }
        if (value instanceof Long) {
            return this.writeLong((Long)value);
        }
        if (value instanceof Float) {
            return this.writeFloat(((Float)value).floatValue());
        }
        if (value instanceof Double) {
            return this.writeDouble((Double)value);
        }
        if (value instanceof Boolean) {
            return this.writeBoolean((boolean)((Boolean)value));
        }
        if (value instanceof byte[]) {
            return this.writeBinary((byte[])value);
        }
        if (value instanceof CharSequence) {
            return this.writeString(String.valueOf(value));
        }
        if (value instanceof Character) {
            return this.writeString(String.valueOf(((Character)value).charValue()));
        }
        if (value instanceof JsonSerializable) {
            return ((JsonSerializable)value).toJson(this);
        }
        if (value instanceof Object[]) {
            this.writeStartArray();
            for (Object element : (Object[])value) {
                this.writeUntyped(element);
            }
            return this.writeEndArray();
        }
        if (value instanceof Iterable) {
            this.writeStartArray();
            for (Object element : (Iterable)value) {
                this.writeUntyped(element);
            }
            return this.writeEndArray();
        }
        if (value instanceof Map) {
            Map mapValue = (Map)value;
            this.writeStartObject();
            for (Map.Entry entry : mapValue.entrySet()) {
                this.writeFieldName(String.valueOf(entry.getKey())).writeUntyped(entry.getValue());
            }
            return this.writeEndObject();
        }
        if (value.getClass() == Object.class) {
            return this.writeStartObject().writeEndObject();
        }
        return this.writeString(String.valueOf(value));
    }
}


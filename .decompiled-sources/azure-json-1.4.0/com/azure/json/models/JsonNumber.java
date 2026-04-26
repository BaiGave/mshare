/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.json.models.JsonElement;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public final class JsonNumber
extends JsonElement {
    private final Number value;

    JsonNumber(String value) throws IllegalArgumentException {
        int length = value.length();
        boolean floatingPoint = false;
        boolean infinity = value.contains("Infinity");
        if (infinity) {
            this.value = Double.parseDouble(value);
            return;
        }
        for (int i = 0; i < length; ++i) {
            char c = value.charAt(i);
            if (c != '.' && c != 'e' && c != 'E') continue;
            floatingPoint = true;
            break;
        }
        this.value = floatingPoint ? (Number)JsonNumber.handleFloatingPoint(value) : (Number)JsonNumber.handleInteger(value);
    }

    private static Number handleFloatingPoint(String value) {
        float f = Float.parseFloat(value);
        if (!Float.isInfinite(f)) {
            return Float.valueOf(f);
        }
        double d = Double.parseDouble(value);
        if (!Double.isInfinite(d)) {
            return d;
        }
        return new BigDecimal(value);
    }

    private static Number handleInteger(String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException failedInteger) {
            try {
                return Long.parseLong(value);
            }
            catch (NumberFormatException failedLong) {
                failedLong.addSuppressed(failedInteger);
                try {
                    return new BigInteger(value);
                }
                catch (NumberFormatException failedBigDecimal) {
                    failedBigDecimal.addSuppressed(failedLong);
                    throw failedBigDecimal;
                }
            }
        }
    }

    public JsonNumber(Number value) {
        this.value = Objects.requireNonNull(value, "JsonNumber cannot represent a null value.");
    }

    public Number getValue() {
        return this.value;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeRawValue(this.value.toString());
    }

    public static JsonNumber fromJson(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }
        if (token != JsonToken.NUMBER) {
            throw new IllegalStateException("JsonReader is pointing to an invalid token for deserialization. Token was: " + (Object)((Object)token) + ".");
        }
        return new JsonNumber(jsonReader.getString());
    }

    @Override
    public String toJsonString() throws IOException {
        return this.value.toString();
    }
}


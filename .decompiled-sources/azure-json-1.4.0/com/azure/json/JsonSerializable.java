/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.StringBuilderWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface JsonSerializable<T extends JsonSerializable<T>> {
    public JsonWriter toJson(JsonWriter var1) throws IOException;

    default public void toJson(OutputStream outputStream) throws IOException {
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream);){
            this.toJson(jsonWriter).flush();
        }
    }

    default public void toJson(Writer writer) throws IOException {
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer);){
            this.toJson(jsonWriter).flush();
        }
    }

    default public String toJsonString() throws IOException {
        StringBuilderWriter writer = new StringBuilderWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(writer);){
            this.toJson(jsonWriter).flush();
            String string = writer.toString();
            return string;
        }
    }

    default public byte[] toJsonBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream);){
            this.toJson(jsonWriter).flush();
            byte[] byArray = outputStream.toByteArray();
            return byArray;
        }
    }

    public static <T extends JsonSerializable<T>> T fromJson(JsonReader jsonReader) throws IOException {
        throw new UnsupportedOperationException("Implementation of JsonSerializable must define this factory method.");
    }

    public static <T extends JsonSerializable<T>> T fromJson(String string) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(string);){
            T t = JsonSerializable.fromJson(jsonReader);
            return t;
        }
    }

    public static <T extends JsonSerializable<T>> T fromJson(byte[] bytes) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(bytes);){
            T t = JsonSerializable.fromJson(jsonReader);
            return t;
        }
    }

    public static <T extends JsonSerializable<T>> T fromJson(InputStream inputStream) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(inputStream);){
            T t = JsonSerializable.fromJson(jsonReader);
            return t;
        }
    }

    public static <T extends JsonSerializable<T>> T fromJson(Reader reader) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(reader);){
            T t = JsonSerializable.fromJson(jsonReader);
            return t;
        }
    }
}


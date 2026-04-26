/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.DefaultJsonReader;
import com.azure.json.implementation.DefaultJsonWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public final class DefaultJsonProvider
implements JsonProvider {
    private static final String JSON_READER_EXCEPTION = "Both 'json' and 'options' must be passed as non-null to create an instance of JsonReader.";
    private static final String JSON_WRITER_EXCEPTION = "Both 'json' and 'options' must be passed as non-null to create an instance of JsonWriter.";

    @Override
    public JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        DefaultJsonProvider.validate(json, options, JSON_READER_EXCEPTION);
        return DefaultJsonReader.fromBytes(json, options);
    }

    @Override
    public JsonReader createReader(String json, JsonOptions options) throws IOException {
        DefaultJsonProvider.validate(json, options, JSON_READER_EXCEPTION);
        return DefaultJsonReader.fromString(json, options);
    }

    @Override
    public JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        DefaultJsonProvider.validate(json, options, JSON_READER_EXCEPTION);
        return DefaultJsonReader.fromStream(json, options);
    }

    @Override
    public JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        DefaultJsonProvider.validate(json, options, JSON_READER_EXCEPTION);
        return DefaultJsonReader.fromReader(json, options);
    }

    @Override
    public JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        DefaultJsonProvider.validate(json, options, JSON_WRITER_EXCEPTION);
        return DefaultJsonWriter.toStream(json, options);
    }

    @Override
    public JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        DefaultJsonProvider.validate(json, options, JSON_WRITER_EXCEPTION);
        return DefaultJsonWriter.toWriter(json, options);
    }

    private static void validate(Object json, JsonOptions options, String exceptionMessage) {
        if (json == null || options == null) {
            throw new NullPointerException(exceptionMessage);
        }
    }
}


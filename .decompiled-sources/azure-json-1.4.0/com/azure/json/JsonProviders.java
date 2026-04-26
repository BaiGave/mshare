/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.json.implementation.DefaultJsonProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

public final class JsonProviders {
    private static final JsonOptions DEFAULT_OPTIONS;
    private static final JsonProvider JSON_PROVIDER;

    private JsonProviders() {
    }

    public static JsonReader createReader(byte[] json) throws IOException {
        return JsonProviders.createReader(json, DEFAULT_OPTIONS);
    }

    public static JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createReader(json, options);
    }

    public static JsonReader createReader(String json) throws IOException {
        return JsonProviders.createReader(json, DEFAULT_OPTIONS);
    }

    public static JsonReader createReader(String json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createReader(json, options);
    }

    public static JsonReader createReader(InputStream json) throws IOException {
        return JsonProviders.createReader(json, DEFAULT_OPTIONS);
    }

    public static JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createReader(json, options);
    }

    public static JsonReader createReader(Reader json) throws IOException {
        return JsonProviders.createReader(json, DEFAULT_OPTIONS);
    }

    public static JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createReader(json, options);
    }

    public static JsonWriter createWriter(OutputStream json) throws IOException {
        return JsonProviders.createWriter(json, DEFAULT_OPTIONS);
    }

    public static JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createWriter(json, options);
    }

    public static JsonWriter createWriter(Writer json) throws IOException {
        return JsonProviders.createWriter(json, DEFAULT_OPTIONS);
    }

    public static JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        return JSON_PROVIDER.createWriter(json, options);
    }

    static {
        JsonProvider implementation;
        DEFAULT_OPTIONS = new JsonOptions();
        ServiceLoader<JsonProvider> serviceLoader = ServiceLoader.load(JsonProvider.class, JsonProvider.class.getClassLoader());
        ArrayList<String> implementationNames = new ArrayList<String>();
        Iterator<JsonProvider> it = serviceLoader.iterator();
        if (it.hasNext()) {
            implementation = it.next();
            implementationNames.add(implementation.getClass().getName());
            JSON_PROVIDER = implementation;
        } else {
            JSON_PROVIDER = new DefaultJsonProvider();
        }
        while (it.hasNext()) {
            implementation = it.next();
            implementationNames.add(implementation.getClass().getName());
        }
        if (implementationNames.size() > 1) {
            throw new IllegalStateException("More than one implementation of 'com.azure.json.JsonProvider' was found on the classpath. At this time 'azure-json' only supports one implementation being on the classpath. Remove all implementations, except the one that should be used during runtime, from 'META-INF/services/com.azure.json.JsonProvider'. Found implementations were: " + String.join((CharSequence)", ", implementationNames));
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

import com.azure.json.JsonOptions;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public interface JsonProvider {
    public JsonReader createReader(byte[] var1, JsonOptions var2) throws IOException;

    public JsonReader createReader(String var1, JsonOptions var2) throws IOException;

    public JsonReader createReader(InputStream var1, JsonOptions var2) throws IOException;

    public JsonReader createReader(Reader var1, JsonOptions var2) throws IOException;

    public JsonWriter createWriter(OutputStream var1, JsonOptions var2) throws IOException;

    public JsonWriter createWriter(Writer var1, JsonOptions var2) throws IOException;
}


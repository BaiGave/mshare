/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonEncoding;
import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.Versioned;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;

public abstract class TokenStreamFactory
implements Versioned,
Serializable {
    private static final long serialVersionUID = 2L;

    public abstract boolean canHandleBinaryNatively();

    public abstract String getFormatName();

    public abstract boolean isEnabled(JsonParser.Feature var1);

    public abstract boolean isEnabled(JsonGenerator.Feature var1);

    public abstract JsonParser createParser(byte[] var1) throws IOException;

    public abstract JsonParser createParser(byte[] var1, int var2, int var3) throws IOException;

    public abstract JsonParser createParser(File var1) throws IOException;

    public abstract JsonParser createParser(InputStream var1) throws IOException;

    public abstract JsonParser createParser(Reader var1) throws IOException;

    public abstract JsonParser createParser(String var1) throws IOException;

    public abstract JsonParser createParser(URL var1) throws IOException;

    public abstract JsonGenerator createGenerator(OutputStream var1) throws IOException;

    public abstract JsonGenerator createGenerator(OutputStream var1, JsonEncoding var2) throws IOException;

    public abstract JsonGenerator createGenerator(Writer var1) throws IOException;

    protected InputStream _optimizedStreamFromURL(URL url) throws IOException {
        String path;
        String host;
        if ("file".equals(url.getProtocol()) && ((host = url.getHost()) == null || host.isEmpty()) && (path = url.getPath()).indexOf(37) < 0) {
            return new FileInputStream(url.getPath());
        }
        return url.openStream();
    }
}


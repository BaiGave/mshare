/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.TreeCodec;
import com.azure.json.implementation.jackson.core.TreeNode;
import com.azure.json.implementation.jackson.core.Version;
import com.azure.json.implementation.jackson.core.Versioned;
import com.azure.json.implementation.jackson.core.type.TypeReference;
import java.io.IOException;

public abstract class ObjectCodec
extends TreeCodec
implements Versioned {
    protected ObjectCodec() {
    }

    @Override
    public abstract Version version();

    public abstract <T> T readValue(JsonParser var1, Class<T> var2) throws IOException;

    public abstract <T> T readValue(JsonParser var1, TypeReference<T> var2) throws IOException;

    public abstract void writeValue(JsonGenerator var1, Object var2) throws IOException;

    @Override
    public abstract void writeTree(JsonGenerator var1, TreeNode var2) throws IOException;

    @Deprecated
    public JsonFactory getJsonFactory() {
        return this.getFactory();
    }

    public JsonFactory getFactory() {
        return this.getJsonFactory();
    }
}


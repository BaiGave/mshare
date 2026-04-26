/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.TreeNode;
import java.io.IOException;

public abstract class TreeCodec {
    public abstract void writeTree(JsonGenerator var1, TreeNode var2) throws IOException;
}


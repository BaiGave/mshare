/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.util;

public interface JacksonFeature {
    public boolean enabledByDefault();

    public int getMask();

    public boolean enabledIn(int var1);
}


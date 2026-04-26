/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.util.JacksonFeature;

public interface FormatFeature
extends JacksonFeature {
    @Override
    public boolean enabledByDefault();

    @Override
    public int getMask();

    @Override
    public boolean enabledIn(int var1);
}


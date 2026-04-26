/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

import java.io.IOException;

@FunctionalInterface
public interface ReadValueCallback<T, R> {
    public R read(T var1) throws IOException;
}


/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

import java.io.IOException;

@FunctionalInterface
public interface WriteValueCallback<T, U> {
    public void write(T var1, U var2) throws IOException;
}


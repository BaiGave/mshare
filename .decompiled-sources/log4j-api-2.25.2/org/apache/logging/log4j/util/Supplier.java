/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.util.InternalApi;

@FunctionalInterface
@InternalApi
public interface Supplier<T>
extends java.util.function.Supplier<T> {
    @Override
    public T get();
}


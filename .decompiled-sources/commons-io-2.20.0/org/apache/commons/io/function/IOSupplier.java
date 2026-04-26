/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Supplier;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOSupplier<T> {
    default public Supplier<T> asSupplier() {
        return this::getUnchecked;
    }

    public T get() throws IOException;

    default public T getUnchecked() throws UncheckedIOException {
        return Uncheck.get(this);
    }
}


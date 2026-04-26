/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOBooleanSupplier {
    default public BooleanSupplier asBooleanSupplier() {
        return () -> Uncheck.getAsBoolean(this);
    }

    public boolean getAsBoolean() throws IOException;
}


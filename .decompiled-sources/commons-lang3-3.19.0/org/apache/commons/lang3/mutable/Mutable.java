/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.mutable;

import java.util.function.Supplier;

public interface Mutable<T>
extends Supplier<T> {
    @Override
    default public T get() {
        return this.getValue();
    }

    @Deprecated
    public T getValue();

    public void setValue(T var1);
}


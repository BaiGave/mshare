/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface FailableByteConsumer<E extends Throwable> {
    public static final FailableByteConsumer NOP = t -> {};

    public static <E extends Throwable> FailableByteConsumer<E> nop() {
        return NOP;
    }

    public void accept(byte var1) throws E;

    default public FailableByteConsumer<E> andThen(FailableByteConsumer<E> after) {
        Objects.requireNonNull(after);
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }
}


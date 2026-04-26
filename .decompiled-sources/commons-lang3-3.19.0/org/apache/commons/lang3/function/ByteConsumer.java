/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

import java.util.Objects;

@FunctionalInterface
public interface ByteConsumer {
    public static final ByteConsumer NOP = t -> {};

    public static ByteConsumer nop() {
        return NOP;
    }

    public void accept(byte var1);

    default public ByteConsumer andThen(ByteConsumer after) {
        Objects.requireNonNull(after);
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import org.apache.commons.io.function.Uncheck;

@FunctionalInterface
public interface IOIntConsumer {
    public static final IOIntConsumer NOOP = i -> {};

    public void accept(int var1) throws IOException;

    default public IOIntConsumer andThen(IOIntConsumer after) {
        Objects.requireNonNull(after);
        return i -> {
            this.accept(i);
            after.accept(i);
        };
    }

    default public Consumer<Integer> asConsumer() {
        return i -> Uncheck.accept(this, i);
    }

    default public IntConsumer asIntConsumer() {
        return i -> Uncheck.accept(this, i);
    }
}


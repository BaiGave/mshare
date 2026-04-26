/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Objects;
import org.apache.commons.io.function.IOConsumer;
import org.apache.commons.io.function.IOIterator;
import org.apache.commons.io.function.IOSpliterator;
import org.apache.commons.io.function.IOSpliteratorAdapter;
import org.apache.commons.io.function.UncheckedIOIterable;

public interface IOIterable<T> {
    default public void forEach(IOConsumer<? super T> action) throws IOException {
        this.iterator().forEachRemaining(Objects.requireNonNull(action));
    }

    public IOIterator<T> iterator();

    default public IOSpliterator<T> spliterator() {
        return IOSpliteratorAdapter.adapt(new UncheckedIOIterable(this).spliterator());
    }

    public Iterable<T> unwrap();
}


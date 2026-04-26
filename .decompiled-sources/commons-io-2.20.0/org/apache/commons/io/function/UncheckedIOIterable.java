/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.function;

import java.util.Iterator;
import java.util.Objects;
import org.apache.commons.io.function.IOIterable;
import org.apache.commons.io.function.UncheckedIOIterator;

final class UncheckedIOIterable<E>
implements Iterable<E> {
    private final IOIterable<E> delegate;

    UncheckedIOIterable(IOIterable<E> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Iterator<E> iterator() {
        return new UncheckedIOIterator<E>(this.delegate.iterator());
    }
}


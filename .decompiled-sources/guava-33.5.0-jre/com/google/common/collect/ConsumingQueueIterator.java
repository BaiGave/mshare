/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import java.util.Queue;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class ConsumingQueueIterator<T>
extends AbstractIterator<T> {
    private final Queue<T> queue;

    ConsumingQueueIterator(Queue<T> queue) {
        this.queue = Preconditions.checkNotNull(queue);
    }

    @Override
    protected @Nullable T computeNext() {
        if (this.queue.isEmpty()) {
            return this.endOfData();
        }
        return this.queue.remove();
    }
}


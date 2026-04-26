/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.build.AbstractStreamBuilder;
import org.apache.commons.io.output.QueueOutputStream;

public class QueueInputStream
extends InputStream {
    private final BlockingQueue<Integer> blockingQueue;
    private final long timeoutNanos;

    public static Builder builder() {
        return new Builder();
    }

    public QueueInputStream() {
        this(new LinkedBlockingQueue<Integer>());
    }

    @Deprecated
    public QueueInputStream(BlockingQueue<Integer> blockingQueue) {
        this(QueueInputStream.builder().setBlockingQueue(blockingQueue));
    }

    private QueueInputStream(Builder builder) {
        this.blockingQueue = Objects.requireNonNull(builder.blockingQueue, "blockingQueue");
        this.timeoutNanos = Objects.requireNonNull(builder.timeout, "timeout").toNanos();
    }

    BlockingQueue<Integer> getBlockingQueue() {
        return this.blockingQueue;
    }

    Duration getTimeout() {
        return Duration.ofNanos(this.timeoutNanos);
    }

    public QueueOutputStream newQueueOutputStream() {
        return new QueueOutputStream(this.blockingQueue);
    }

    @Override
    public int read() {
        try {
            Integer value = this.blockingQueue.poll(this.timeoutNanos, TimeUnit.NANOSECONDS);
            return value == null ? -1 : 0xFF & value;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int read(byte[] b, int offset, int length) {
        if (b == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || length < 0 || length > b.length - offset) {
            throw new IndexOutOfBoundsException(String.format("Range [%d, %<d + %d) out of bounds for length %d", offset, length, b.length));
        }
        if (length == 0) {
            return 0;
        }
        ArrayList<Integer> drain = new ArrayList<Integer>(Math.min(length, this.blockingQueue.size()));
        this.blockingQueue.drainTo(drain, length);
        if (drain.isEmpty()) {
            int value = this.read();
            if (value == -1) {
                return -1;
            }
            drain.add(value);
            this.blockingQueue.drainTo(drain, length - 1);
        }
        int i = 0;
        for (Integer value : drain) {
            b[offset + i] = (byte)(0xFF & value);
            ++i;
        }
        return i;
    }

    public static class Builder
    extends AbstractStreamBuilder<QueueInputStream, Builder> {
        private BlockingQueue<Integer> blockingQueue = new LinkedBlockingQueue<Integer>();
        private Duration timeout = Duration.ZERO;

        @Override
        public QueueInputStream get() {
            return new QueueInputStream(this);
        }

        public Builder setBlockingQueue(BlockingQueue<Integer> blockingQueue) {
            this.blockingQueue = blockingQueue != null ? blockingQueue : new LinkedBlockingQueue();
            return this;
        }

        public Builder setTimeout(Duration timeout) {
            if (timeout != null && timeout.toNanos() < 0L) {
                throw new IllegalArgumentException("timeout must not be negative");
            }
            this.timeout = timeout != null ? timeout : Duration.ZERO;
            return this;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.input.ProxyInputStream;

public final class ThrottledInputStream
extends CountingInputStream {
    private final double maxBytesPerSecond;
    private final long startTime = System.currentTimeMillis();
    private Duration totalSleepDuration = Duration.ZERO;

    public static Builder builder() {
        return new Builder();
    }

    static long toSleepMillis(long bytesRead, long elapsedMillis, double maxBytesPerSec) {
        if (bytesRead <= 0L || maxBytesPerSec <= 0.0 || elapsedMillis == 0L) {
            return 0L;
        }
        long millis = (long)((double)bytesRead / maxBytesPerSec * 1000.0 - (double)elapsedMillis);
        if (millis <= 0L) {
            return 0L;
        }
        return millis;
    }

    private ThrottledInputStream(Builder builder) throws IOException {
        super(builder);
        if (builder.maxBytesPerSecond <= 0.0) {
            throw new IllegalArgumentException("Bandwidth " + builder.maxBytesPerSecond + " is invalid.");
        }
        this.maxBytesPerSecond = builder.maxBytesPerSecond;
    }

    @Override
    protected void beforeRead(int n) throws IOException {
        this.throttle();
    }

    private long getBytesPerSecond() {
        long elapsedSeconds = (System.currentTimeMillis() - this.startTime) / 1000L;
        if (elapsedSeconds == 0L) {
            return this.getByteCount();
        }
        return this.getByteCount() / elapsedSeconds;
    }

    double getMaxBytesPerSecond() {
        return this.maxBytesPerSecond;
    }

    private long getSleepMillis() {
        return ThrottledInputStream.toSleepMillis(this.getByteCount(), System.currentTimeMillis() - this.startTime, this.maxBytesPerSecond);
    }

    Duration getTotalSleepDuration() {
        return this.totalSleepDuration;
    }

    private void throttle() throws InterruptedIOException {
        long sleepMillis = this.getSleepMillis();
        if (sleepMillis > 0L) {
            this.totalSleepDuration = this.totalSleepDuration.plus(sleepMillis, ChronoUnit.MILLIS);
            try {
                TimeUnit.MILLISECONDS.sleep(sleepMillis);
            }
            catch (InterruptedException e) {
                throw new InterruptedIOException("Thread aborted");
            }
        }
    }

    public String toString() {
        return "ThrottledInputStream[bytesRead=" + this.getByteCount() + ", maxBytesPerSec=" + this.maxBytesPerSecond + ", bytesPerSec=" + this.getBytesPerSecond() + ", totalSleepDuration=" + this.totalSleepDuration + ']';
    }

    public static class Builder
    extends ProxyInputStream.AbstractBuilder<ThrottledInputStream, Builder> {
        private double maxBytesPerSecond = Double.MAX_VALUE;

        @Override
        public ThrottledInputStream get() throws IOException {
            return new ThrottledInputStream(this);
        }

        double getMaxBytesPerSecond() {
            return this.maxBytesPerSecond;
        }

        public Builder setMaxBytes(long value, ChronoUnit chronoUnit) {
            this.setMaxBytes(value, chronoUnit.getDuration());
            return (Builder)this.asThis();
        }

        Builder setMaxBytes(long value, Duration duration) {
            this.setMaxBytesPerSecond((double)Objects.requireNonNull(duration, "duration").toMillis() / 1000.0 * (double)value);
            return (Builder)this.asThis();
        }

        private Builder setMaxBytesPerSecond(double maxBytesPerSecond) {
            if (maxBytesPerSecond <= 0.0) {
                throw new IllegalArgumentException("Bandwidth " + maxBytesPerSecond + " must be > 0.");
            }
            this.maxBytesPerSecond = maxBytesPerSecond;
            return (Builder)this.asThis();
        }

        public void setMaxBytesPerSecond(long maxBytesPerSecond) {
            this.setMaxBytesPerSecond((double)maxBytesPerSecond);
        }
    }
}


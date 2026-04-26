/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.time.Duration;
import java.time.Instant;

public final class ThreadUtils {
    private static int getNanosOfMilli(Duration duration) {
        return duration.getNano() % 1000000;
    }

    public static void sleep(Duration duration) throws InterruptedException {
        try {
            long nowNano;
            long nanoStart = System.nanoTime();
            long finishNanos = nanoStart + duration.toNanos();
            Duration remainingDuration = duration;
            do {
                Thread.sleep(remainingDuration.toMillis(), ThreadUtils.getNanosOfMilli(remainingDuration));
                nowNano = System.nanoTime();
                remainingDuration = Duration.ofNanos(finishNanos - nowNano);
            } while (nowNano - finishNanos < 0L);
        }
        catch (ArithmeticException e) {
            Instant finishInstant = Instant.now().plus(duration);
            Duration remainingDuration = duration;
            do {
                Thread.sleep(remainingDuration.toMillis(), ThreadUtils.getNanosOfMilli(remainingDuration));
            } while (!(remainingDuration = Duration.between(Instant.now(), finishInstant)).isNegative());
        }
    }

    @Deprecated
    public ThreadUtils() {
    }
}


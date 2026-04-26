/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.io;

import java.time.Duration;
import org.apache.commons.io.ThreadUtils;

final class ThreadMonitor
implements Runnable {
    private final Thread thread;
    private final Duration timeout;

    static Thread start(Duration timeout) {
        return ThreadMonitor.start(Thread.currentThread(), timeout);
    }

    static Thread start(Thread thread, Duration timeout) {
        if (timeout.isZero() || timeout.isNegative()) {
            return null;
        }
        Thread monitor = new Thread(new ThreadMonitor(thread, timeout), ThreadMonitor.class.getSimpleName());
        monitor.setDaemon(true);
        monitor.start();
        return monitor;
    }

    static void stop(Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }

    private ThreadMonitor(Thread thread, Duration timeout) {
        this.thread = thread;
        this.timeout = timeout;
    }

    @Override
    public void run() {
        try {
            ThreadUtils.sleep(this.timeout);
            this.thread.interrupt();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.status;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusListener;

public class StatusConsoleListener
implements StatusListener {
    private final Lock lock = new ReentrantLock();
    private final Level initialLevel;
    private final PrintStream initialStream;
    private volatile Level level;
    private volatile PrintStream stream;

    public StatusConsoleListener(Level level) {
        this(level, System.out);
    }

    public StatusConsoleListener(Level level, PrintStream stream) {
        this.initialLevel = this.level = Objects.requireNonNull(level, "level");
        this.initialStream = this.stream = Objects.requireNonNull(stream, "stream");
    }

    public void setLevel(Level level) {
        Objects.requireNonNull(level, "level");
        if (!this.level.equals(level)) {
            this.lock.lock();
            try {
                this.level = level;
            }
            finally {
                this.lock.unlock();
            }
        }
    }

    public void setStream(PrintStream stream) {
        Objects.requireNonNull(stream, "stream");
        if (this.stream != stream) {
            PrintStream oldStream = null;
            this.lock.lock();
            try {
                if (this.stream != stream) {
                    oldStream = this.stream;
                    this.stream = stream;
                }
            }
            finally {
                this.lock.unlock();
            }
            if (oldStream != null) {
                StatusConsoleListener.closeNonSystemStream(oldStream);
            }
        }
    }

    @Override
    public Level getStatusLevel() {
        return this.level;
    }

    @Override
    public void log(StatusData data) {
        Objects.requireNonNull(data, "data");
        String formattedStatus = data.getFormattedStatus();
        this.stream.println(formattedStatus);
    }

    @Deprecated
    public void setFilters(String ... filters) {
    }

    @Override
    public void close() {
        PrintStream oldStream;
        this.lock.lock();
        try {
            oldStream = this.stream;
            this.stream = this.initialStream;
            this.level = this.initialLevel;
        }
        finally {
            this.lock.unlock();
        }
        StatusConsoleListener.closeNonSystemStream(oldStream);
    }

    private static void closeNonSystemStream(OutputStream stream) {
        if (stream != System.out && stream != System.err) {
            try {
                stream.close();
            }
            catch (IOException error) {
                error.printStackTrace(System.err);
            }
        }
    }
}


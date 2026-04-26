/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.log.ConsoleLogHandler;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;
import net.fabricmc.loader.impl.util.log.LogLevel;

final class BuiltinLogHandler
extends ConsoleLogHandler {
    private static final String DEFAULT_LOG_FILE = "fabricloader.log";
    private boolean configured;
    private boolean enableOutput;
    private List<ReplayEntry> buffer = new ArrayList<ReplayEntry>();
    private final Thread shutdownHook = new ShutdownHook();

    BuiltinLogHandler() {
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void log(long time, LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay, boolean wasSuppressed) {
        boolean output;
        BuiltinLogHandler builtinLogHandler = this;
        synchronized (builtinLogHandler) {
            if (this.enableOutput) {
                output = true;
            } else if (level.isLessThan(LogLevel.ERROR)) {
                output = false;
            } else {
                this.startOutput();
                output = true;
            }
            if (this.buffer != null) {
                this.buffer.add(new ReplayEntry(time, level, category, msg, exc));
            }
        }
        if (output) {
            super.log(time, level, category, msg, exc, fromReplay, wasSuppressed);
        }
    }

    private void startOutput() {
        if (this.enableOutput) {
            return;
        }
        if (this.buffer != null) {
            for (int i = 0; i < this.buffer.size(); ++i) {
                ReplayEntry entry = this.buffer.get(i);
                super.log(entry.time, entry.level, entry.category, entry.msg, entry.exc, true, true);
            }
        }
        this.enableOutput = true;
    }

    @Override
    public void close() {
        Thread shutdownHook = this.shutdownHook;
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            }
            catch (IllegalStateException illegalStateException) {
                // empty catch block
            }
        }
    }

    synchronized void configure(boolean buffer, boolean output) {
        if (!buffer && !output) {
            throw new IllegalArgumentException("can't both disable buffering and the output");
        }
        if (output) {
            this.startOutput();
        } else {
            this.enableOutput = false;
        }
        if (buffer) {
            if (this.buffer == null) {
                this.buffer = new ArrayList<ReplayEntry>();
            }
        } else {
            this.buffer = null;
        }
        this.configured = true;
    }

    synchronized void finishConfig() {
        if (!this.configured) {
            this.configure(false, true);
        }
    }

    synchronized boolean replay(LogHandler target) {
        if (this.buffer == null || this.buffer.isEmpty()) {
            return false;
        }
        for (int i = 0; i < this.buffer.size(); ++i) {
            ReplayEntry entry = this.buffer.get(i);
            target.log(entry.time, entry.level, entry.category, entry.msg, entry.exc, true, !this.enableOutput);
        }
        return true;
    }

    private final class ShutdownHook
    extends Thread {
        ShutdownHook() {
            super("BuiltinLogHandler shutdown hook");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            BuiltinLogHandler builtinLogHandler = BuiltinLogHandler.this;
            synchronized (builtinLogHandler) {
                String fileName;
                if (BuiltinLogHandler.this.buffer == null || BuiltinLogHandler.this.buffer.isEmpty()) {
                    return;
                }
                if (!BuiltinLogHandler.this.enableOutput) {
                    BuiltinLogHandler.this.enableOutput = true;
                    for (int i = 0; i < BuiltinLogHandler.this.buffer.size(); ++i) {
                        ReplayEntry entry = (ReplayEntry)BuiltinLogHandler.this.buffer.get(i);
                        BuiltinLogHandler.super.log(entry.time, entry.level, entry.category, entry.msg, entry.exc, true, true);
                    }
                }
                if ((fileName = System.getProperty("fabric.log.file", BuiltinLogHandler.DEFAULT_LOG_FILE)).isEmpty()) {
                    return;
                }
                try {
                    Path file = LoaderUtil.normalizePath(Paths.get(fileName, new String[0]));
                    Files.createDirectories(file.getParent(), new FileAttribute[0]);
                    try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);){
                        for (int i = 0; i < BuiltinLogHandler.this.buffer.size(); ++i) {
                            ReplayEntry entry = (ReplayEntry)BuiltinLogHandler.this.buffer.get(i);
                            writer.write(ConsoleLogHandler.formatLog(entry.time, entry.level, entry.category, entry.msg, entry.exc));
                        }
                    }
                }
                catch (IOException e) {
                    System.err.printf("Error saving log: %s", e);
                }
            }
        }
    }

    private static final class ReplayEntry {
        final long time;
        final LogLevel level;
        final LogCategory category;
        final String msg;
        final Throwable exc;

        ReplayEntry(long time, LogLevel level, LogCategory category, String msg, Throwable exc) {
            this.time = time;
            this.level = level;
            this.category = category;
            this.msg = msg;
            this.exc = exc;
        }
    }
}


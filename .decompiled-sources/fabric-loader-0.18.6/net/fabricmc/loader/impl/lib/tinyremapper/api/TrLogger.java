/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.api;

public interface TrLogger {
    public void log(Level var1, String var2);

    default public void log(Level level, String message, Object ... args) {
        this.log(level, String.format(message, args));
    }

    default public void warn(String message) {
        this.log(Level.WARN, message);
    }

    default public void warn(String message, Object ... args) {
        this.log(Level.WARN, message, args);
    }

    default public void error(String message) {
        this.log(Level.ERROR, message);
    }

    default public void error(String message, Object ... args) {
        this.log(Level.ERROR, message, args);
    }

    public static enum Level {
        DEBUG,
        INFO,
        WARN,
        ERROR;

    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util.log;

import java.util.Locale;

public enum LogLevel {
    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE;


    public boolean isLessThan(LogLevel level) {
        return this.ordinal() > level.ordinal();
    }

    public static LogLevel getDefault() {
        String val = System.getProperty("fabric.log.level");
        if (val == null) {
            return INFO;
        }
        LogLevel ret = LogLevel.valueOf(val.toUpperCase(Locale.ENGLISH));
        if (ret == null) {
            throw new IllegalArgumentException("invalid log level: " + val);
        }
        return ret;
    }
}


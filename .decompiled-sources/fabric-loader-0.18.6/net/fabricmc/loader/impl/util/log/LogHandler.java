/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util.log;

import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogLevel;

public interface LogHandler {
    public void log(long var1, LogLevel var3, LogCategory var4, String var5, Throwable var6, boolean var7, boolean var8);

    public boolean shouldLog(LogLevel var1, LogCategory var2);

    public void close();
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.simple.internal;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.NoOpThreadContextMap;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class SimpleProvider
extends Provider {
    private final ThreadContextMap threadContextMap;

    public SimpleProvider() {
        super(null, "2.6.0");
        this.threadContextMap = Config.INSTANCE.showContextMap ? super.getThreadContextMapInstance() : NoOpThreadContextMap.INSTANCE;
    }

    @Override
    public LoggerContextFactory getLoggerContextFactory() {
        return SimpleLoggerContextFactory.INSTANCE;
    }

    @Override
    public ThreadContextMap getThreadContextMapInstance() {
        return this.threadContextMap;
    }

    public static final class Config {
        private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS zzz";
        private static final String SYSTEM_PREFIX = "org.apache.logging.log4j.simplelog.";
        private static final String SYSTEM_OUT = "system.out";
        private static final String SYSTEM_ERR = "system.err";
        public static final Config INSTANCE = new Config();
        public final PropertiesUtil props = new PropertiesUtil("log4j2.simplelog.properties");
        public final boolean showContextMap = this.props.getBooleanProperty("org.apache.logging.log4j.simplelog.showContextMap", false);
        public final boolean showLogName = this.props.getBooleanProperty("org.apache.logging.log4j.simplelog.showlogname", false);
        public final boolean showShortName = this.props.getBooleanProperty("org.apache.logging.log4j.simplelog.showShortLogname", true);
        public final boolean showDateTime = this.props.getBooleanProperty("org.apache.logging.log4j.simplelog.showdatetime", false);
        public final Level defaultLevel;
        public final @Nullable String dateTimeFormat;
        public final PrintStream stream;

        private Config() {
            PrintStream ps;
            String lvl = this.props.getStringProperty("org.apache.logging.log4j.simplelog.level");
            this.defaultLevel = Level.toLevel(lvl, Level.ERROR);
            this.dateTimeFormat = this.showDateTime ? this.props.getStringProperty("org.apache.logging.log4j.simplelog.dateTimeFormat", DEFAULT_DATE_TIME_FORMAT) : null;
            String fileName = this.props.getStringProperty("org.apache.logging.log4j.simplelog.logFile", SYSTEM_ERR);
            if (SYSTEM_ERR.equalsIgnoreCase(fileName)) {
                ps = System.err;
            } else if (SYSTEM_OUT.equalsIgnoreCase(fileName)) {
                ps = System.out;
            } else {
                try {
                    ps = new PrintStream(new FileOutputStream(fileName));
                }
                catch (FileNotFoundException fnfe) {
                    ps = System.err;
                }
            }
            this.stream = ps;
        }
    }
}


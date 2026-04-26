/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.slf4j.Log4jEventBuilder;
import org.apache.logging.slf4j.Log4jLoggerFactory;
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.LoggingEventBuilder;

public class Log4jLogger
implements LocationAwareLogger,
Serializable {
    public static final String FQCN = Log4jLogger.class.getName();
    private static final long serialVersionUID = 7869000638091304316L;
    private transient ExtendedLogger logger;
    private final String name;
    private transient Log4jMarkerFactory markerFactory;

    public Log4jLogger(Log4jMarkerFactory markerFactory, ExtendedLogger logger, String name) {
        this.markerFactory = markerFactory;
        this.logger = logger;
        this.name = name;
    }

    @Override
    public void trace(String format) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, null, format);
    }

    @Override
    public void trace(String format, Object o) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, (org.apache.logging.log4j.Marker)null, format, o);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, (org.apache.logging.log4j.Marker)null, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object ... args) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, (org.apache.logging.log4j.Marker)null, format, args);
    }

    @Override
    public void trace(String format, Throwable t) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, (org.apache.logging.log4j.Marker)null, format, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return this.logger.isEnabled(Level.TRACE, null, null);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.logger.isEnabled(Level.TRACE, this.markerFactory.getLog4jMarker(marker), null);
    }

    @Override
    public void trace(Marker marker, String s) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, this.markerFactory.getLog4jMarker(marker), s);
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, this.markerFactory.getLog4jMarker(marker), s, o);
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, this.markerFactory.getLog4jMarker(marker), s, o, o1);
    }

    @Override
    public void trace(Marker marker, String s, Object ... objects) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, this.markerFactory.getLog4jMarker(marker), s, objects);
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.TRACE, this.markerFactory.getLog4jMarker(marker), s, throwable);
    }

    @Override
    public void debug(String format) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, null, format);
    }

    @Override
    public void debug(String format, Object o) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, (org.apache.logging.log4j.Marker)null, format, o);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, (org.apache.logging.log4j.Marker)null, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object ... args) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, (org.apache.logging.log4j.Marker)null, format, args);
    }

    @Override
    public void debug(String format, Throwable t) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, (org.apache.logging.log4j.Marker)null, format, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.logger.isEnabled(Level.DEBUG, null, null);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.logger.isEnabled(Level.DEBUG, this.markerFactory.getLog4jMarker(marker), null);
    }

    @Override
    public void debug(Marker marker, String s) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, this.markerFactory.getLog4jMarker(marker), s);
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, this.markerFactory.getLog4jMarker(marker), s, o);
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, this.markerFactory.getLog4jMarker(marker), s, o, o1);
    }

    @Override
    public void debug(Marker marker, String s, Object ... objects) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, this.markerFactory.getLog4jMarker(marker), s, objects);
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.DEBUG, this.markerFactory.getLog4jMarker(marker), s, throwable);
    }

    @Override
    public void info(String format) {
        this.logger.logIfEnabled(FQCN, Level.INFO, null, format);
    }

    @Override
    public void info(String format, Object o) {
        this.logger.logIfEnabled(FQCN, Level.INFO, (org.apache.logging.log4j.Marker)null, format, o);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        this.logger.logIfEnabled(FQCN, Level.INFO, (org.apache.logging.log4j.Marker)null, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object ... args) {
        this.logger.logIfEnabled(FQCN, Level.INFO, (org.apache.logging.log4j.Marker)null, format, args);
    }

    @Override
    public void info(String format, Throwable t) {
        this.logger.logIfEnabled(FQCN, Level.INFO, (org.apache.logging.log4j.Marker)null, format, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.logger.isEnabled(Level.INFO, null, null);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.logger.isEnabled(Level.INFO, this.markerFactory.getLog4jMarker(marker), null);
    }

    @Override
    public void info(Marker marker, String s) {
        this.logger.logIfEnabled(FQCN, Level.INFO, this.markerFactory.getLog4jMarker(marker), s);
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        this.logger.logIfEnabled(FQCN, Level.INFO, this.markerFactory.getLog4jMarker(marker), s, o);
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        this.logger.logIfEnabled(FQCN, Level.INFO, this.markerFactory.getLog4jMarker(marker), s, o, o1);
    }

    @Override
    public void info(Marker marker, String s, Object ... objects) {
        this.logger.logIfEnabled(FQCN, Level.INFO, this.markerFactory.getLog4jMarker(marker), s, objects);
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.INFO, this.markerFactory.getLog4jMarker(marker), s, throwable);
    }

    @Override
    public void warn(String format) {
        this.logger.logIfEnabled(FQCN, Level.WARN, null, format);
    }

    @Override
    public void warn(String format, Object o) {
        this.logger.logIfEnabled(FQCN, Level.WARN, (org.apache.logging.log4j.Marker)null, format, o);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        this.logger.logIfEnabled(FQCN, Level.WARN, (org.apache.logging.log4j.Marker)null, format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object ... args) {
        this.logger.logIfEnabled(FQCN, Level.WARN, (org.apache.logging.log4j.Marker)null, format, args);
    }

    @Override
    public void warn(String format, Throwable t) {
        this.logger.logIfEnabled(FQCN, Level.WARN, (org.apache.logging.log4j.Marker)null, format, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.logger.isEnabled(Level.WARN, null, null);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.logger.isEnabled(Level.WARN, this.markerFactory.getLog4jMarker(marker), null);
    }

    @Override
    public void warn(Marker marker, String s) {
        this.logger.logIfEnabled(FQCN, Level.WARN, this.markerFactory.getLog4jMarker(marker), s);
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        this.logger.logIfEnabled(FQCN, Level.WARN, this.markerFactory.getLog4jMarker(marker), s, o);
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        this.logger.logIfEnabled(FQCN, Level.WARN, this.markerFactory.getLog4jMarker(marker), s, o, o1);
    }

    @Override
    public void warn(Marker marker, String s, Object ... objects) {
        this.logger.logIfEnabled(FQCN, Level.WARN, this.markerFactory.getLog4jMarker(marker), s, objects);
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.WARN, this.markerFactory.getLog4jMarker(marker), s, throwable);
    }

    @Override
    public void error(String format) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, null, format);
    }

    @Override
    public void error(String format, Object o) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, (org.apache.logging.log4j.Marker)null, format, o);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, (org.apache.logging.log4j.Marker)null, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object ... args) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, (org.apache.logging.log4j.Marker)null, format, args);
    }

    @Override
    public void error(String format, Throwable t) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, (org.apache.logging.log4j.Marker)null, format, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.logger.isEnabled(Level.ERROR, null, null);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.logger.isEnabled(Level.ERROR, this.markerFactory.getLog4jMarker(marker), null);
    }

    @Override
    public void error(Marker marker, String s) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, this.markerFactory.getLog4jMarker(marker), s);
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, this.markerFactory.getLog4jMarker(marker), s, o);
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, this.markerFactory.getLog4jMarker(marker), s, o, o1);
    }

    @Override
    public void error(Marker marker, String s, Object ... objects) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, this.markerFactory.getLog4jMarker(marker), s, objects);
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        this.logger.logIfEnabled(FQCN, Level.ERROR, this.markerFactory.getLog4jMarker(marker), s, throwable);
    }

    @Override
    public void log(Marker marker, String fqcn, int level, String message, Object[] params, Throwable throwable) {
        Throwable actualThrowable;
        Message msg;
        org.apache.logging.log4j.Marker log4jMarker;
        Level log4jLevel = Log4jLogger.getLevel(level);
        if (!this.logger.isEnabled(log4jLevel, log4jMarker = this.markerFactory.getLog4jMarker(marker), message, params)) {
            return;
        }
        if (params == null) {
            msg = new SimpleMessage(message);
            actualThrowable = throwable;
        } else {
            msg = new ParameterizedMessage(message, params, throwable);
            actualThrowable = throwable != null ? throwable : msg.getThrowable();
        }
        this.logger.logMessage(fqcn, log4jLevel, log4jMarker, msg, actualThrowable);
    }

    @Override
    public String getName() {
        return this.name;
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        this.logger = LogManager.getContext().getLogger(this.name);
        this.markerFactory = ((Log4jLoggerFactory)LoggerFactory.getILoggerFactory()).getMarkerFactory();
    }

    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
        aOutputStream.defaultWriteObject();
    }

    private static Level getLevel(int i) {
        switch (i) {
            case 0: {
                return Level.TRACE;
            }
            case 10: {
                return Level.DEBUG;
            }
            case 20: {
                return Level.INFO;
            }
            case 30: {
                return Level.WARN;
            }
            case 40: {
                return Level.ERROR;
            }
        }
        return Level.ERROR;
    }

    @Override
    public LoggingEventBuilder makeLoggingEventBuilder(org.slf4j.event.Level level) {
        Level log4jLevel = Log4jLogger.getLevel(level.toInt());
        return new Log4jEventBuilder(this.markerFactory, this.logger, log4jLevel);
    }

    @Override
    public boolean isEnabledForLevel(org.slf4j.event.Level level) {
        return this.logger.isEnabled(Log4jLogger.getLevel(level.toInt()));
    }
}


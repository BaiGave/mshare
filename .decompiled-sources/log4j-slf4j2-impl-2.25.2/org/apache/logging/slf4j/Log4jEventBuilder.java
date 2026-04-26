/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.logging.log4j.BridgeAware;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.spi.CallerBoundaryAware;
import org.slf4j.spi.LoggingEventBuilder;

public class Log4jEventBuilder
implements LoggingEventBuilder,
CallerBoundaryAware {
    private static final String FQCN = Log4jEventBuilder.class.getName();
    private final Log4jMarkerFactory markerFactory;
    private final Logger logger;
    private final List<Object> arguments = new ArrayList<Object>();
    private String message = null;
    private org.apache.logging.log4j.Marker marker = null;
    private Throwable throwable = null;
    private Map<String, String> keyValuePairs = null;
    private final Level level;
    private String fqcn = FQCN;

    public Log4jEventBuilder(Log4jMarkerFactory markerFactory, Logger logger, Level level) {
        this.markerFactory = markerFactory;
        this.logger = logger;
        this.level = level;
    }

    @Override
    public LoggingEventBuilder setCause(Throwable cause) {
        this.throwable = cause;
        return this;
    }

    @Override
    public LoggingEventBuilder addMarker(Marker marker) {
        this.marker = this.markerFactory.getLog4jMarker(marker);
        return this;
    }

    @Override
    public LoggingEventBuilder addArgument(Object p) {
        this.arguments.add(p);
        return this;
    }

    @Override
    public LoggingEventBuilder addArgument(Supplier<?> objectSupplier) {
        this.arguments.add(objectSupplier.get());
        return this;
    }

    @Override
    public LoggingEventBuilder addKeyValue(String key, Object value) {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = new HashMap<String, String>();
        }
        this.keyValuePairs.put(key, String.valueOf(value));
        return this;
    }

    @Override
    public LoggingEventBuilder addKeyValue(String key, Supplier<Object> valueSupplier) {
        if (this.keyValuePairs == null) {
            this.keyValuePairs = new HashMap<String, String>();
        }
        this.keyValuePairs.put(key, String.valueOf(valueSupplier.get()));
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(Supplier<String> messageSupplier) {
        this.message = messageSupplier.get();
        return this;
    }

    @Override
    public void log() {
        LogBuilder logBuilder = this.logger.atLevel(this.level).withMarker(this.marker).withThrowable(this.throwable);
        if (logBuilder instanceof BridgeAware) {
            ((BridgeAware)((Object)logBuilder)).setEntryPoint(this.fqcn);
        }
        if (this.keyValuePairs == null || this.keyValuePairs.isEmpty()) {
            logBuilder.log(this.message, this.arguments.toArray());
        } else {
            try (CloseableThreadContext.Instance c = CloseableThreadContext.putAll(this.keyValuePairs);){
                logBuilder.log(this.message, this.arguments.toArray());
            }
        }
    }

    @Override
    public void log(String message) {
        this.setMessage(message);
        this.log();
    }

    @Override
    public void log(String message, Object arg) {
        this.setMessage(message);
        this.addArgument(arg);
        this.log();
    }

    @Override
    public void log(String message, Object arg0, Object arg1) {
        this.setMessage(message);
        this.addArgument(arg0);
        this.addArgument(arg1);
        this.log();
    }

    @Override
    public void log(String message, Object ... args) {
        this.setMessage(message);
        for (Object arg : args) {
            this.addArgument(arg);
        }
        this.log();
    }

    @Override
    public void log(Supplier<String> messageSupplier) {
        this.setMessage(messageSupplier);
        this.log();
    }

    @Override
    public void setCallerBoundary(String fqcn) {
        this.fqcn = fqcn;
    }
}


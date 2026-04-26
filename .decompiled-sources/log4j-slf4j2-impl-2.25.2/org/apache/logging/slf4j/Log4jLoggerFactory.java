/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j;

import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.spi.AbstractLoggerAdapter;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.slf4j.Log4jLogger;
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class Log4jLoggerFactory
extends AbstractLoggerAdapter<Logger>
implements ILoggerFactory {
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    private static final String SLF4J_PACKAGE = "org.slf4j";
    private static final Predicate<Class<?>> CALLER_PREDICATE = clazz -> !AbstractLoggerAdapter.class.equals(clazz) && !clazz.getName().startsWith(SLF4J_PACKAGE);
    private static final String TO_SLF4J_CONTEXT = "org.apache.logging.slf4j.SLF4JLoggerContext";
    private final Log4jMarkerFactory markerFactory;

    public Log4jLoggerFactory(Log4jMarkerFactory markerFactory) {
        this.markerFactory = markerFactory;
    }

    @Override
    protected Logger newLogger(String name, LoggerContext context) {
        String key = "ROOT".equals(name) ? "" : name;
        return new Log4jLogger(this.markerFactory, this.validateContext(context).getLogger(key), name);
    }

    @Override
    protected LoggerContext getContext() {
        Class<?> anchor = LogManager.getFactory().isClassLoaderDependent() ? StackLocatorUtil.getCallerClass(Log4jLoggerFactory.class, CALLER_PREDICATE) : null;
        LOGGER.trace("Log4jLoggerFactory.getContext() found anchor {}", (Object)anchor);
        return anchor == null ? LogManager.getContext(false) : this.getContext(anchor);
    }

    Log4jMarkerFactory getMarkerFactory() {
        return this.markerFactory;
    }

    private LoggerContext validateContext(LoggerContext context) {
        if (TO_SLF4J_CONTEXT.equals(context.getClass().getName())) {
            throw new LoggingException("log4j-slf4j2-impl cannot be present with log4j-to-slf4j");
        }
        return context;
    }
}


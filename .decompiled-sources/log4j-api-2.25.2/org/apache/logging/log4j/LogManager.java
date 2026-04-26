/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j;

import java.net.URI;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.internal.LogManagerStatus;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.Terminable;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.ProviderUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;

public class LogManager {
    @Deprecated
    public static final String FACTORY_PROPERTY_NAME = "log4j2.loggerContextFactory";
    public static final String ROOT_LOGGER_NAME = "";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String FQCN = LogManager.class.getName();
    private static volatile LoggerContextFactory factory = ProviderUtil.getProvider().getLoggerContextFactory();

    protected LogManager() {
    }

    public static boolean exists(String name) {
        return LogManager.getContext().hasLogger(name);
    }

    public static LoggerContext getContext() {
        try {
            return factory.getContext(FQCN, null, null, true);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(FQCN, null, null, true);
        }
    }

    public static LoggerContext getContext(boolean currentContext) {
        try {
            return factory.getContext(FQCN, null, null, currentContext, null, null);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(FQCN, null, null, currentContext, null, null);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext) {
        try {
            return factory.getContext(FQCN, loader, null, currentContext);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(FQCN, loader, null, currentContext);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext, Object externalContext) {
        try {
            return factory.getContext(FQCN, loader, externalContext, currentContext);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(FQCN, loader, externalContext, currentContext);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext, URI configLocation) {
        try {
            return factory.getContext(FQCN, loader, null, currentContext, configLocation, null);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(FQCN, loader, null, currentContext, configLocation, null);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext, Object externalContext, URI configLocation) {
        try {
            return factory.getContext(FQCN, loader, externalContext, currentContext, configLocation, null);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(FQCN, loader, externalContext, currentContext, configLocation, null);
        }
    }

    public static LoggerContext getContext(ClassLoader loader, boolean currentContext, Object externalContext, URI configLocation, String name) {
        try {
            return factory.getContext(FQCN, loader, externalContext, currentContext, configLocation, name);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(FQCN, loader, externalContext, currentContext, configLocation, name);
        }
    }

    protected static LoggerContext getContext(String fqcn, boolean currentContext) {
        try {
            return factory.getContext(fqcn, null, null, currentContext);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(fqcn, null, null, currentContext);
        }
    }

    protected static LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext) {
        try {
            return factory.getContext(fqcn, loader, null, currentContext);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(fqcn, loader, null, currentContext);
        }
    }

    protected static LoggerContext getContext(String fqcn, ClassLoader loader, boolean currentContext, URI configLocation, String name) {
        try {
            return factory.getContext(fqcn, loader, null, currentContext, configLocation, name);
        }
        catch (IllegalStateException ex) {
            LOGGER.warn("{} Using SimpleLogger", (Object)ex.getMessage());
            return SimpleLoggerContextFactory.INSTANCE.getContext(fqcn, loader, null, currentContext);
        }
    }

    public static void shutdown() {
        LogManager.shutdown(false);
    }

    public static void shutdown(boolean currentContext) {
        factory.shutdown(FQCN, null, currentContext, false);
    }

    public static void shutdown(boolean currentContext, boolean allContexts) {
        factory.shutdown(FQCN, null, currentContext, allContexts);
    }

    public static void shutdown(LoggerContext context) {
        if (context instanceof Terminable) {
            ((Terminable)((Object)context)).terminate();
        }
    }

    public static LoggerContextFactory getFactory() {
        return factory;
    }

    public static void setFactory(LoggerContextFactory factory) {
        LogManager.factory = factory;
    }

    public static Logger getFormatterLogger() {
        return LogManager.getFormatterLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getFormatterLogger(Class<?> clazz) {
        return LogManager.getLogger(clazz != null ? clazz : StackLocatorUtil.getCallerClass(2), (MessageFactory)StringFormatterMessageFactory.INSTANCE);
    }

    public static Logger getFormatterLogger(Object value) {
        return LogManager.getLogger(value != null ? value.getClass() : StackLocatorUtil.getCallerClass(2), (MessageFactory)StringFormatterMessageFactory.INSTANCE);
    }

    public static Logger getFormatterLogger(String name) {
        return name == null ? LogManager.getFormatterLogger(StackLocatorUtil.getCallerClass(2)) : LogManager.getLogger(name, (MessageFactory)StringFormatterMessageFactory.INSTANCE);
    }

    private static Class<?> callerClass(Class<?> clazz) {
        if (clazz != null) {
            return clazz;
        }
        Class<?> candidate = StackLocatorUtil.getCallerClass(3);
        if (candidate == null) {
            throw new UnsupportedOperationException("No class provided, and an appropriate one cannot be found.");
        }
        return candidate;
    }

    public static Logger getLogger() {
        return LogManager.getLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(Class<?> clazz) {
        Class<?> cls = LogManager.callerClass(clazz);
        return LogManager.getContext(cls.getClassLoader(), false).getLogger(cls);
    }

    public static Logger getLogger(Class<?> clazz, MessageFactory messageFactory) {
        Class<?> cls = LogManager.callerClass(clazz);
        return LogManager.getContext(cls.getClassLoader(), false).getLogger(cls, messageFactory);
    }

    public static Logger getLogger(MessageFactory messageFactory) {
        return LogManager.getLogger(StackLocatorUtil.getCallerClass(2), messageFactory);
    }

    public static Logger getLogger(Object value) {
        return LogManager.getLogger(value != null ? value.getClass() : StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(Object value, MessageFactory messageFactory) {
        return LogManager.getLogger(value != null ? value.getClass() : StackLocatorUtil.getCallerClass(2), messageFactory);
    }

    public static Logger getLogger(String name) {
        return name != null ? LogManager.getContext(false).getLogger(name) : LogManager.getLogger(StackLocatorUtil.getCallerClass(2));
    }

    public static Logger getLogger(String name, MessageFactory messageFactory) {
        return name != null ? LogManager.getContext(false).getLogger(name, messageFactory) : LogManager.getLogger(StackLocatorUtil.getCallerClass(2), messageFactory);
    }

    protected static Logger getLogger(String fqcn, String name) {
        return factory.getContext(fqcn, null, null, false).getLogger(name);
    }

    public static Logger getRootLogger() {
        return LogManager.getLogger(ROOT_LOGGER_NAME);
    }

    static {
        LogManagerStatus.setInitialized(true);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.ParameterizedMessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class LoggerRegistry<T extends ExtendedLogger> {
    private final Map<String, Map<MessageFactory, T>> loggerByMessageFactoryByName = new HashMap<String, Map<MessageFactory, T>>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = this.lock.readLock();
    private final Lock writeLock = this.lock.writeLock();

    public LoggerRegistry() {
    }

    @Deprecated
    public LoggerRegistry(@Nullable MapFactory<T> mapFactory) {
        this();
    }

    @Deprecated
    public @Nullable T getLogger(String name) {
        Objects.requireNonNull(name, "name");
        return this.getLogger(name, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public @Nullable T getLogger(String name, @Nullable MessageFactory messageFactory) {
        Objects.requireNonNull(name, "name");
        this.readLock.lock();
        try {
            @Nullable Map<MessageFactory, T> loggerByMessageFactory = this.loggerByMessageFactoryByName.get(name);
            MessageFactory effectiveMessageFactory = messageFactory != null ? messageFactory : ParameterizedMessageFactory.INSTANCE;
            ExtendedLogger extendedLogger = loggerByMessageFactory == null ? null : (ExtendedLogger)loggerByMessageFactory.get(effectiveMessageFactory);
            return (T)extendedLogger;
        }
        finally {
            this.readLock.unlock();
        }
    }

    public Collection<T> getLoggers() {
        this.readLock.lock();
        try {
            Collection collection = this.loggerByMessageFactoryByName.values().stream().flatMap(loggerByMessageFactory -> loggerByMessageFactory.values().stream()).collect(Collectors.toList());
            return collection;
        }
        finally {
            this.readLock.unlock();
        }
    }

    public Collection<T> getLoggers(Collection<T> destination) {
        Objects.requireNonNull(destination, "destination");
        destination.addAll(this.getLoggers());
        return destination;
    }

    @Deprecated
    public boolean hasLogger(String name) {
        Objects.requireNonNull(name, "name");
        @Nullable T logger = this.getLogger(name);
        return logger != null;
    }

    public boolean hasLogger(String name, @Nullable MessageFactory messageFactory) {
        Objects.requireNonNull(name, "name");
        @Nullable T logger = this.getLogger(name, messageFactory);
        return logger != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasLogger(String name, Class<? extends MessageFactory> messageFactoryClass) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(messageFactoryClass, "messageFactoryClass");
        this.readLock.lock();
        try {
            boolean bl = this.loggerByMessageFactoryByName.getOrDefault(name, Collections.emptyMap()).keySet().stream().anyMatch(messageFactory -> messageFactoryClass.equals(messageFactory.getClass()));
            return bl;
        }
        finally {
            this.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void putIfAbsent(String name, @Nullable MessageFactory messageFactory, T logger) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(logger, "logger");
        this.writeLock.lock();
        try {
            MessageFactory effectiveMessageFactory = messageFactory != null ? messageFactory : ParameterizedMessageFactory.INSTANCE;
            this.loggerByMessageFactoryByName.computeIfAbsent(name, this::createLoggerRefByMessageFactoryMap).putIfAbsent(effectiveMessageFactory, logger);
            if (!name.equals(logger.getName()) || !effectiveMessageFactory.equals(logger.getMessageFactory())) {
                this.loggerByMessageFactoryByName.computeIfAbsent(logger.getName(), this::createLoggerRefByMessageFactoryMap).putIfAbsent(logger.getMessageFactory(), logger);
            }
        }
        finally {
            this.writeLock.unlock();
        }
    }

    private Map<MessageFactory, T> createLoggerRefByMessageFactoryMap(String ignored) {
        return new WeakHashMap();
    }

    @Deprecated
    public static class WeakMapFactory<T extends ExtendedLogger>
    implements MapFactory<T> {
        @Override
        public Map<String, T> createInnerMap() {
            return new WeakHashMap();
        }

        @Override
        public Map<String, Map<String, T>> createOuterMap() {
            return new WeakHashMap<String, Map<String, T>>();
        }

        @Override
        public void putIfAbsent(Map<String, T> innerMap, String name, T logger) {
            innerMap.put(name, logger);
        }
    }

    @Deprecated
    public static class ConcurrentMapFactory<T extends ExtendedLogger>
    implements MapFactory<T> {
        @Override
        public Map<String, T> createInnerMap() {
            return new ConcurrentHashMap();
        }

        @Override
        public Map<String, Map<String, T>> createOuterMap() {
            return new ConcurrentHashMap<String, Map<String, T>>();
        }

        @Override
        public void putIfAbsent(Map<String, T> innerMap, String name, T logger) {
            innerMap.putIfAbsent(name, logger);
        }
    }

    @Deprecated
    public static interface MapFactory<T extends ExtendedLogger> {
        public Map<String, T> createInnerMap();

        public Map<String, Map<String, T>> createOuterMap();

        public void putIfAbsent(Map<String, T> var1, String var2, T var3);
    }
}


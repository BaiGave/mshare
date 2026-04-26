/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.status.StatusLogger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class InternalLoggerRegistry {
    private final Map<MessageFactory, Map<String, WeakReference<Logger>>> loggerRefByNameByMessageFactory = new WeakHashMap<MessageFactory, Map<String, WeakReference<Logger>>>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = this.lock.readLock();
    private final Lock writeLock = this.lock.writeLock();
    private final ReferenceQueue<Logger> staleLoggerRefs = new ReferenceQueue();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void expungeStaleEntries() {
        Reference<Logger> loggerRef = this.staleLoggerRefs.poll();
        if (loggerRef != null) {
            this.writeLock.lock();
            try {
                while (this.staleLoggerRefs.poll() != null) {
                }
                Iterator<Map.Entry<MessageFactory, Map<String, WeakReference<Logger>>>> loggerRefByNameByMessageFactoryEntryIt = this.loggerRefByNameByMessageFactory.entrySet().iterator();
                while (loggerRefByNameByMessageFactoryEntryIt.hasNext()) {
                    Map.Entry<MessageFactory, Map<String, WeakReference<Logger>>> loggerRefByNameByMessageFactoryEntry = loggerRefByNameByMessageFactoryEntryIt.next();
                    Map<String, WeakReference<Logger>> loggerRefByName = loggerRefByNameByMessageFactoryEntry.getValue();
                    loggerRefByName.values().removeIf(weakRef -> weakRef.get() == null);
                    if (!loggerRefByName.isEmpty()) continue;
                    loggerRefByNameByMessageFactoryEntryIt.remove();
                }
            }
            finally {
                this.writeLock.unlock();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public @Nullable Logger getLogger(String name, MessageFactory messageFactory) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(messageFactory, "messageFactory");
        this.expungeStaleEntries();
        this.readLock.lock();
        try {
            WeakReference<Logger> loggerRef;
            Map<String, WeakReference<Logger>> loggerRefByName = this.loggerRefByNameByMessageFactory.get(messageFactory);
            if (loggerRefByName != null && (loggerRef = loggerRefByName.get(name)) != null) {
                Logger logger = (Logger)loggerRef.get();
                return logger;
            }
            Logger logger = null;
            return logger;
        }
        finally {
            this.readLock.unlock();
        }
    }

    public Collection<Logger> getLoggers() {
        this.expungeStaleEntries();
        this.readLock.lock();
        try {
            Collection collection = this.loggerRefByNameByMessageFactory.values().stream().flatMap(loggerRefByName -> loggerRefByName.values().stream()).flatMap(loggerRef -> {
                @Nullable Logger logger = (Logger)loggerRef.get();
                return logger != null ? Stream.of(logger) : Stream.empty();
            }).collect(Collectors.toList());
            return collection;
        }
        finally {
            this.readLock.unlock();
        }
    }

    public boolean hasLogger(String name, MessageFactory messageFactory) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(messageFactory, "messageFactory");
        return this.getLogger(name, messageFactory) != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean hasLogger(String name, Class<? extends MessageFactory> messageFactoryClass) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(messageFactoryClass, "messageFactoryClass");
        this.expungeStaleEntries();
        this.readLock.lock();
        try {
            boolean bl = this.loggerRefByNameByMessageFactory.entrySet().stream().filter(entry -> messageFactoryClass.equals(((MessageFactory)entry.getKey()).getClass())).anyMatch(entry -> ((Map)entry.getValue()).containsKey(name));
            return bl;
        }
        finally {
            this.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Logger computeIfAbsent(String name, MessageFactory messageFactory, BiFunction<String, MessageFactory, Logger> loggerSupplier) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(messageFactory, "messageFactory");
        Objects.requireNonNull(loggerSupplier, "loggerSupplier");
        @Nullable Logger logger = this.getLogger(name, messageFactory);
        if (logger != null) {
            return logger;
        }
        Logger newLogger = loggerSupplier.apply(name, messageFactory);
        String loggerName = newLogger.getName();
        Object loggerMessageFactory = newLogger.getMessageFactory();
        if (!loggerName.equals(name) || !loggerMessageFactory.equals(messageFactory)) {
            StatusLogger.getLogger().error("Newly registered logger with name `{}` and message factory `{}`, is requested to be associated with a different name `{}` or message factory `{}`.\nEffectively the message factory of the logger will be used and the other one will be ignored.\nThis generally hints a problem at the logger context implementation.\nPlease report this using the Log4j project issue tracker.", (Object)loggerName, loggerMessageFactory, (Object)name, (Object)messageFactory);
        }
        this.writeLock.lock();
        try {
            WeakReference<Logger> loggerRef;
            Map<String, WeakReference<Logger>> loggerRefByName = this.loggerRefByNameByMessageFactory.get(messageFactory);
            if (loggerRefByName == null) {
                loggerRefByName = new HashMap<String, WeakReference<Logger>>();
                this.loggerRefByNameByMessageFactory.put(messageFactory, loggerRefByName);
            }
            if ((loggerRef = loggerRefByName.get(name)) == null || (logger = (Logger)loggerRef.get()) == null) {
                logger = newLogger;
                loggerRefByName.put(name, new WeakReference<Logger>(logger, this.staleLoggerRefs));
            }
            Logger logger2 = logger;
            return logger2;
        }
        finally {
            this.writeLock.unlock();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import aQute.bnd.annotation.spi.ServiceProvider;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.context.internal.GarbageFreeSortedArrayThreadContextMap;
import org.apache.logging.log4j.core.impl.Log4jContextFactory;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.NoOpThreadContextMap;
import org.apache.logging.log4j.spi.Provider;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Lazy;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.jspecify.annotations.NullMarked;

@NullMarked
@ServiceProvider(value=Provider.class, resolution="optional")
public class Log4jProvider
extends Provider {
    private static final String NO_OP_CONTEXT_MAP = "NoOp";
    private static final String WEB_APP_CONTEXT_MAP = "WebApp";
    private static final String GARBAGE_FREE_CONTEXT_MAP = "GarbageFree";
    private static final String DISABLE_CONTEXT_MAP = "log4j2.disableThreadContextMap";
    private static final String DISABLE_THREAD_CONTEXT = "log4j2.disableThreadContext";
    private static final String THREAD_CONTEXT_MAP_PROPERTY = "log4j2.threadContextMap";
    private static final String GC_FREE_THREAD_CONTEXT_PROPERTY = "log4j2.garbagefree.threadContextMap";
    private static final String WEB_APP_CLASS_NAME = "org.apache.logging.log4j.spi.DefaultThreadContextMap";
    private static final String GARBAGE_FREE_CLASS_NAME = "org.apache.logging.log4j.core.context.internal.GarbageFreeSortedArrayThreadContextMap";
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final Lazy<LoggerContextFactory> loggerContextFactoryLazy = Lazy.lazy(Log4jContextFactory::new);
    private final Lazy<ThreadContextMap> threadContextMapLazy = Lazy.lazy(this::createThreadContextMap);

    public Log4jProvider() {
        super(10, "2.6.0", Log4jContextFactory.class);
    }

    @Override
    public LoggerContextFactory getLoggerContextFactory() {
        return this.loggerContextFactoryLazy.get();
    }

    @Override
    public ThreadContextMap getThreadContextMapInstance() {
        return this.threadContextMapLazy.get();
    }

    private ThreadContextMap createThreadContextMap() {
        PropertiesUtil props = PropertiesUtil.getProperties();
        if (props.getBooleanProperty(DISABLE_CONTEXT_MAP) || props.getBooleanProperty(DISABLE_THREAD_CONTEXT)) {
            return NoOpThreadContextMap.INSTANCE;
        }
        String threadContextMapClass = props.getStringProperty(THREAD_CONTEXT_MAP_PROPERTY);
        if (threadContextMapClass == null) {
            threadContextMapClass = props.getBooleanProperty(GC_FREE_THREAD_CONTEXT_PROPERTY) ? GARBAGE_FREE_CONTEXT_MAP : WEB_APP_CONTEXT_MAP;
        }
        switch (threadContextMapClass) {
            case "NoOp": {
                return NoOpThreadContextMap.INSTANCE;
            }
            case "WebApp": 
            case "org.apache.logging.log4j.spi.DefaultThreadContextMap": {
                return new DefaultThreadContextMap();
            }
            case "org.apache.logging.log4j.spi.GarbageFreeSortedArrayThreadContextMap": 
            case "GarbageFree": 
            case "org.apache.logging.log4j.core.context.internal.GarbageFreeSortedArrayThreadContextMap": {
                return new GarbageFreeSortedArrayThreadContextMap();
            }
        }
        try {
            return LoaderUtil.newCheckedInstanceOf(threadContextMapClass, ThreadContextMap.class);
        }
        catch (Exception e) {
            LOGGER.error("Unable to create instance of class {}.", (Object)threadContextMapClass, (Object)e);
            LOGGER.warn("Falling back to {}.", (Object)NoOpThreadContextMap.class.getName());
            return NoOpThreadContextMap.INSTANCE;
        }
    }

    void resetThreadContextMap() {
        this.threadContextMapLazy.set(null);
    }
}


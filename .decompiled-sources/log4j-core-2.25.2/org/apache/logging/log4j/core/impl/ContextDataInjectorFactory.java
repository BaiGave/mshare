/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.impl.ThreadContextDataInjector;
import org.apache.logging.log4j.spi.CopyOnWrite;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;
import org.apache.logging.log4j.spi.ReadOnlyThreadContextMap;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;

public class ContextDataInjectorFactory {
    private static final String CONTEXT_DATA_INJECTOR_PROPERTY = "log4j2.ContextDataInjector";

    public static ContextDataInjector createInjector() {
        try {
            return LoaderUtil.newCheckedInstanceOfProperty(CONTEXT_DATA_INJECTOR_PROPERTY, ContextDataInjector.class, ContextDataInjectorFactory::createDefaultInjector);
        }
        catch (ReflectiveOperationException e) {
            StatusLogger.getLogger().warn("Could not create ContextDataInjector: {}", (Object)e.getMessage(), (Object)e);
            return ContextDataInjectorFactory.createDefaultInjector();
        }
    }

    private static ContextDataInjector createDefaultInjector() {
        ReadOnlyThreadContextMap threadContextMap = ThreadContext.getThreadContextMap();
        if (threadContextMap instanceof DefaultThreadContextMap || threadContextMap == null) {
            return new ThreadContextDataInjector.ForDefaultThreadContextMap();
        }
        if (threadContextMap instanceof CopyOnWrite) {
            return new ThreadContextDataInjector.ForCopyOnWriteThreadContextMap();
        }
        return new ThreadContextDataInjector.ForGarbageFreeThreadContextMap();
    }
}


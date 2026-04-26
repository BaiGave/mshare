/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLoggerContextFactory;
import org.apache.logging.log4j.spi.DefaultThreadContextMap;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.apache.logging.log4j.spi.NoOpThreadContextMap;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class Provider {
    protected static final String CURRENT_VERSION = "2.6.0";
    @Deprecated
    public static final String FACTORY_PRIORITY = "FactoryPriority";
    @Deprecated
    public static final String THREAD_CONTEXT_MAP = "ThreadContextMap";
    @Deprecated
    public static final String LOGGER_CONTEXT_FACTORY = "LoggerContextFactory";
    public static final String PROVIDER_PROPERTY_NAME = "log4j.provider";
    private static final String DISABLE_CONTEXT_MAP = "log4j2.disableThreadContextMap";
    private static final String DISABLE_THREAD_CONTEXT = "log4j2.disableThreadContext";
    private static final int DEFAULT_PRIORITY = -1;
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final int priority;
    @Deprecated
    private final @Nullable String className;
    private final @Nullable Class<? extends LoggerContextFactory> loggerContextFactoryClass;
    @Deprecated
    private final @Nullable String threadContextMap;
    private final @Nullable Class<? extends ThreadContextMap> threadContextMapClass;
    private final @Nullable String versions;
    @Deprecated
    private final @Nullable URL url;
    @Deprecated
    private final WeakReference<ClassLoader> classLoader;

    @Deprecated
    public Provider(Properties props, URL url, ClassLoader classLoader) {
        this.url = url;
        this.classLoader = new WeakReference<ClassLoader>(classLoader);
        String weight = props.getProperty(FACTORY_PRIORITY);
        this.priority = weight == null ? -1 : Integer.parseInt(weight);
        this.className = props.getProperty(LOGGER_CONTEXT_FACTORY);
        this.threadContextMap = props.getProperty(THREAD_CONTEXT_MAP);
        this.loggerContextFactoryClass = null;
        this.threadContextMapClass = null;
        this.versions = null;
    }

    public Provider(@Nullable Integer priority, String versions) {
        this(priority, versions, null, null);
    }

    public Provider(@Nullable Integer priority, String versions, @Nullable Class<? extends LoggerContextFactory> loggerContextFactoryClass) {
        this(priority, versions, loggerContextFactoryClass, null);
    }

    public Provider(@Nullable Integer priority, String versions, @Nullable Class<? extends LoggerContextFactory> loggerContextFactoryClass, @Nullable Class<? extends ThreadContextMap> threadContextMapClass) {
        this.priority = priority != null ? priority : -1;
        this.versions = versions;
        this.loggerContextFactoryClass = loggerContextFactoryClass;
        this.threadContextMapClass = threadContextMapClass;
        this.className = null;
        this.threadContextMap = null;
        this.url = null;
        this.classLoader = new WeakReference<Object>(null);
    }

    public String getVersions() {
        return this.versions != null ? this.versions : "";
    }

    public Integer getPriority() {
        return this.priority;
    }

    public @Nullable String getClassName() {
        return this.loggerContextFactoryClass != null ? this.loggerContextFactoryClass.getName() : this.className;
    }

    public @Nullable Class<? extends LoggerContextFactory> loadLoggerContextFactory() {
        if (this.loggerContextFactoryClass != null) {
            return this.loggerContextFactoryClass;
        }
        String className = this.getClassName();
        ClassLoader loader = (ClassLoader)this.classLoader.get();
        if (loader == null || className == null) {
            return null;
        }
        try {
            Class<?> clazz = loader.loadClass(className);
            if (LoggerContextFactory.class.isAssignableFrom(clazz)) {
                return clazz.asSubclass(LoggerContextFactory.class);
            }
            LOGGER.error("Class {} specified in {} does not extend {}", (Object)className, (Object)this.getUrl(), (Object)LoggerContextFactory.class.getName());
        }
        catch (Exception e) {
            LOGGER.error("Unable to create class {} specified in {}", (Object)className, (Object)this.getUrl(), (Object)e);
        }
        return null;
    }

    public LoggerContextFactory getLoggerContextFactory() {
        Class<? extends LoggerContextFactory> implementation = this.loadLoggerContextFactory();
        if (implementation != null) {
            try {
                return LoaderUtil.newInstanceOf(implementation);
            }
            catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to instantiate logger context factory {}.", (Object)implementation.getName(), (Object)e);
            }
        }
        LOGGER.error("Falling back to simple logger context factory: {}", (Object)SimpleLoggerContextFactory.class.getName());
        return SimpleLoggerContextFactory.INSTANCE;
    }

    public @Nullable String getThreadContextMap() {
        return this.threadContextMapClass != null ? this.threadContextMapClass.getName() : this.threadContextMap;
    }

    public @Nullable Class<? extends ThreadContextMap> loadThreadContextMap() {
        if (this.threadContextMapClass != null) {
            return this.threadContextMapClass;
        }
        String threadContextMap = this.getThreadContextMap();
        ClassLoader loader = (ClassLoader)this.classLoader.get();
        if (loader == null || threadContextMap == null) {
            return null;
        }
        try {
            Class<?> clazz = loader.loadClass(threadContextMap);
            if (ThreadContextMap.class.isAssignableFrom(clazz)) {
                return clazz.asSubclass(ThreadContextMap.class);
            }
            LOGGER.error("Class {} specified in {} does not extend {}", (Object)threadContextMap, (Object)this.getUrl(), (Object)ThreadContextMap.class.getName());
        }
        catch (Exception e) {
            LOGGER.error("Unable to load class {} specified in {}", (Object)threadContextMap, (Object)this.url, (Object)e);
        }
        return null;
    }

    public ThreadContextMap getThreadContextMapInstance() {
        PropertiesUtil props;
        Class<? extends ThreadContextMap> implementation = this.loadThreadContextMap();
        if (implementation != null) {
            try {
                return LoaderUtil.newInstanceOf(implementation);
            }
            catch (ReflectiveOperationException e) {
                LOGGER.error("Failed to instantiate logger context factory {}.", (Object)implementation.getName(), (Object)e);
            }
        }
        return (props = PropertiesUtil.getProperties()).getBooleanProperty(DISABLE_CONTEXT_MAP) || props.getBooleanProperty(DISABLE_THREAD_CONTEXT) ? NoOpThreadContextMap.INSTANCE : new DefaultThreadContextMap();
    }

    @Deprecated
    public @Nullable URL getUrl() {
        return this.url;
    }

    public String toString() {
        String loggerContextFactory;
        String threadContextMap;
        StringBuilder result = new StringBuilder("Provider '").append(this.getClass().getName()).append("'");
        if (this.priority != -1) {
            result.append("\n\tpriority = ").append(this.priority);
        }
        if ((threadContextMap = this.getThreadContextMap()) != null) {
            result.append("\n\tthreadContextMap = ").append(threadContextMap);
        }
        if ((loggerContextFactory = this.getClassName()) != null) {
            result.append("\n\tloggerContextFactory = ").append(loggerContextFactory);
        }
        if (this.url != null) {
            result.append("\n\turl = ").append(this.url);
        }
        if (Provider.class.equals(this.getClass())) {
            ClassLoader loader = (ClassLoader)this.classLoader.get();
            if (loader == null) {
                result.append("\n\tclassLoader = null or not reachable");
            } else {
                result.append("\n\tclassLoader = ").append(loader);
            }
        }
        return result.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Provider) {
            Provider provider = (Provider)o;
            return Objects.equals(this.priority, provider.priority) && Objects.equals(this.className, provider.className) && Objects.equals(this.loggerContextFactoryClass, provider.loggerContextFactoryClass) && Objects.equals(this.versions, provider.versions);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.priority, this.className, this.loggerContextFactoryClass, this.versions);
    }
}


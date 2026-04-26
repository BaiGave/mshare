/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import aQute.bnd.annotation.spi.ServiceProvider;
import java.util.Collection;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertySource;

@ServiceProvider(value=PropertySource.class, resolution="optional")
public class SystemPropertiesPropertySource
implements PropertySource {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final int DEFAULT_PRIORITY = 0;
    private static final String PREFIX = "log4j2.";
    private static final PropertySource INSTANCE = new SystemPropertiesPropertySource();

    public static PropertySource provider() {
        return INSTANCE;
    }

    public static String getSystemProperty(String key, String defaultValue) {
        String value = INSTANCE.getProperty(key);
        return value != null ? value : defaultValue;
    }

    private static void logException(SecurityException error) {
        LOGGER.error("The Java system properties are not available to Log4j due to security restrictions.", (Throwable)error);
    }

    private static void logException(SecurityException error, String key) {
        LOGGER.error("The Java system property {} is not available to Log4j due to security restrictions.", (Object)key, (Object)error);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void forEach(BiConsumer<String, String> action) {
        Object[] properties;
        try {
            properties = System.getProperties();
        }
        catch (SecurityException e) {
            SystemPropertiesPropertySource.logException(e);
            return;
        }
        Object[] objectArray = properties;
        synchronized (properties) {
            Object[] keySet = properties.keySet().toArray();
            // ** MonitorExit[var4_5] (shouldn't be in output)
            for (Object key : keySet) {
                String keyStr = Objects.toString(key, null);
                action.accept(keyStr, properties.getProperty(keyStr));
            }
            return;
        }
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        return PREFIX + PropertySource.Util.joinAsCamelCase(tokens);
    }

    @Override
    public Collection<String> getPropertyNames() {
        try {
            return System.getProperties().stringPropertyNames();
        }
        catch (SecurityException e) {
            SystemPropertiesPropertySource.logException(e);
            return PropertySource.super.getPropertyNames();
        }
    }

    @Override
    public String getProperty(String key) {
        try {
            return System.getProperty(key);
        }
        catch (SecurityException e) {
            SystemPropertiesPropertySource.logException(e, key);
            return PropertySource.super.getProperty(key);
        }
    }

    @Override
    public boolean containsProperty(String key) {
        return this.getProperty(key) != null;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import aQute.bnd.annotation.spi.ServiceProvider;
import java.util.Collection;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.PropertySource;

@ServiceProvider(value=PropertySource.class, resolution="optional")
public class EnvironmentPropertySource
implements PropertySource {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String PREFIX = "LOG4J_";
    private static final int DEFAULT_PRIORITY = 100;
    private static final PropertySource INSTANCE = new EnvironmentPropertySource();

    public static PropertySource provider() {
        return INSTANCE;
    }

    @Override
    public int getPriority() {
        return 100;
    }

    private static void logException(SecurityException error) {
        LOGGER.error("The environment variables are not available to Log4j due to security restrictions.", (Throwable)error);
    }

    private static void logException(SecurityException error, String key) {
        LOGGER.error("The environment variable {} is not available to Log4j due to security restrictions.", (Object)key, (Object)error);
    }

    @Override
    public void forEach(BiConsumer<String, String> action) {
        Map<String, String> getenv;
        try {
            getenv = System.getenv();
        }
        catch (SecurityException e) {
            EnvironmentPropertySource.logException(e);
            return;
        }
        for (Map.Entry<String, String> entry : getenv.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(PREFIX)) continue;
            action.accept(key.substring(PREFIX.length()), entry.getValue());
        }
    }

    @Override
    public CharSequence getNormalForm(Iterable<? extends CharSequence> tokens) {
        StringBuilder sb = new StringBuilder("LOG4J");
        boolean empty = true;
        for (CharSequence charSequence : tokens) {
            empty = false;
            sb.append('_');
            for (int i = 0; i < charSequence.length(); ++i) {
                sb.append(Character.toUpperCase(charSequence.charAt(i)));
            }
        }
        return empty ? null : sb.toString();
    }

    @Override
    public Collection<String> getPropertyNames() {
        try {
            return System.getenv().keySet();
        }
        catch (SecurityException e) {
            EnvironmentPropertySource.logException(e);
            return PropertySource.super.getPropertyNames();
        }
    }

    @Override
    public String getProperty(String key) {
        try {
            return System.getenv(key);
        }
        catch (SecurityException e) {
            EnvironmentPropertySource.logException(e, key);
            return PropertySource.super.getProperty(key);
        }
    }

    @Override
    public boolean containsProperty(String key) {
        try {
            return System.getenv().containsKey(key);
        }
        catch (SecurityException e) {
            EnvironmentPropertySource.logException(e, key);
            return PropertySource.super.containsProperty(key);
        }
    }
}


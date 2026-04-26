/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.MapLookup;
import org.apache.logging.log4j.core.util.internal.SystemUtils;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="jvmrunargs", category="Lookup")
public class JmxRuntimeInputArgumentsLookup
extends MapLookup {
    private static final Logger LOGGER = StatusLogger.getLogger();
    public static final JmxRuntimeInputArgumentsLookup JMX_SINGLETON = new JmxRuntimeInputArgumentsLookup();

    public JmxRuntimeInputArgumentsLookup() {
        this(JmxRuntimeInputArgumentsLookup.getMapFromJmx());
    }

    public JmxRuntimeInputArgumentsLookup(Map<String, String> map) {
        super(map);
    }

    @Override
    public String lookup(LogEvent ignored, String key) {
        if (key == null) {
            return null;
        }
        Map<String, String> map = this.getMap();
        return map == null ? null : map.get(key);
    }

    private static Map<String, String> getMapFromJmx() {
        if (!SystemUtils.isOsAndroid()) {
            try {
                return MapLookup.toMap(ManagementFactory.getRuntimeMXBean().getInputArguments());
            }
            catch (LinkageError e) {
                LOGGER.warn("Failed to get JMX arguments from JVM.", (Throwable)e);
            }
        }
        return Collections.emptyMap();
    }
}


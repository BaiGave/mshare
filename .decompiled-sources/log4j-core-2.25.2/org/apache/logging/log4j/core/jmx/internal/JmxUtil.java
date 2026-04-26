/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.jmx.internal;

import org.apache.logging.log4j.util.PropertiesUtil;

public final class JmxUtil {
    public static boolean isJmxDisabled() {
        return PropertiesUtil.getProperties().getBooleanProperty("log4j2.disable.jmx", true);
    }

    private JmxUtil() {
    }
}


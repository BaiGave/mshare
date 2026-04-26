/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

public final class SystemUtils {
    private static final Logger LOGGER = StatusLogger.getLogger();

    private static String getJavaVendor() {
        try {
            return System.getProperty("java.vendor");
        }
        catch (SecurityException e) {
            LOGGER.warn("Unable to determine Java vendor.", (Throwable)e);
            return "Unknown";
        }
    }

    public static boolean isOsAndroid() {
        return SystemUtils.getJavaVendor().contains("Android");
    }

    public static boolean isGraalVm() {
        try {
            return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
        }
        catch (SecurityException e) {
            LOGGER.debug("Unable to determine if the current runtime is GraalVM.", (Throwable)e);
            return false;
        }
    }

    private SystemUtils() {
    }
}


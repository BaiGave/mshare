/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

final class PrivateSecurityManagerStackTraceUtil {
    private static final PrivateSecurityManager SECURITY_MANAGER;

    private static boolean isCapable(PrivateSecurityManager candidate) {
        if (candidate == null) {
            return false;
        }
        try {
            Class<?>[] result = candidate.getClassContext();
            return result != null && result.length != 0;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    private static PrivateSecurityManager createPrivateSecurityManager() {
        PrivateSecurityManager psm;
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(new RuntimePermission("createSecurityManager"));
            }
            psm = new PrivateSecurityManager();
        }
        catch (SecurityException ignored) {
            psm = null;
        }
        return psm;
    }

    private PrivateSecurityManagerStackTraceUtil() {
    }

    static boolean isEnabled() {
        return SECURITY_MANAGER != null;
    }

    static Deque<Class<?>> getCurrentStackTrace() {
        Class<?>[] array = SECURITY_MANAGER.getClassContext();
        ArrayDeque classes = new ArrayDeque(array.length);
        Collections.addAll(classes, array);
        return classes;
    }

    static {
        PrivateSecurityManager candidate = PrivateSecurityManagerStackTraceUtil.createPrivateSecurityManager();
        SECURITY_MANAGER = PrivateSecurityManagerStackTraceUtil.isCapable(candidate) ? candidate : null;
    }

    private static final class PrivateSecurityManager
    extends SecurityManager {
        private PrivateSecurityManager() {
        }

        @Override
        protected Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public class UnsafeUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static Method cleanerMethod;
    private static Method cleanMethod;

    public static void clean(ByteBuffer bb) throws Exception {
        if (cleanerMethod != null && cleanMethod != null && bb.isDirect()) {
            cleanMethod.invoke(cleanerMethod.invoke((Object)bb, new Object[0]), new Object[0]);
        }
    }

    static {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>(){

                @Override
                public Void run() throws ReflectiveOperationException, SecurityException {
                    ByteBuffer direct = ByteBuffer.allocateDirect(1);
                    cleanerMethod = direct.getClass().getDeclaredMethod("cleaner", new Class[0]);
                    cleanerMethod.setAccessible(true);
                    Object cleaner = cleanerMethod.invoke((Object)direct, new Object[0]);
                    cleanMethod = cleaner.getClass().getMethod("clean", new Class[0]);
                    return null;
                }
            });
        }
        catch (PrivilegedActionException e) {
            Exception wrapped = e.getException();
            if (wrapped instanceof SecurityException) {
                throw (SecurityException)wrapped;
            }
            LOGGER.warn("sun.misc.Cleaner#clean() is not accessible. This will impact memory usage.", (Throwable)wrapped);
            cleanerMethod = null;
            cleanMethod = null;
        }
    }
}


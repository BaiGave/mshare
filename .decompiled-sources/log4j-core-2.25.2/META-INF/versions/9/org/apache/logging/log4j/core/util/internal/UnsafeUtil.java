/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import sun.misc.Unsafe;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public class UnsafeUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final Unsafe unsafe = UnsafeUtil.findUnsafe();

    private static Unsafe findUnsafe() {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Unsafe>(){

                @Override
                public Unsafe run() throws ReflectiveOperationException, SecurityException {
                    Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
                    unsafeField.setAccessible(true);
                    return (Unsafe)unsafeField.get(null);
                }
            });
        }
        catch (PrivilegedActionException e) {
            Exception wrapped = e.getException();
            if (wrapped instanceof SecurityException) {
                throw (SecurityException)wrapped;
            }
            LOGGER.warn("sun.misc.Unsafe is not available. This will impact memory usage.", (Throwable)e);
            return null;
        }
    }

    public static void clean(ByteBuffer bb) throws Exception {
        if (unsafe != null && bb.isDirect()) {
            unsafe.invokeCleaner(bb);
        }
    }
}


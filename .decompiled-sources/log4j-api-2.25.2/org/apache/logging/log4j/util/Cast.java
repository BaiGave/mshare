/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.util.InternalApi;

@InternalApi
public final class Cast {
    public static <T> T cast(Object o) {
        if (o == null) {
            return null;
        }
        Object t = o;
        return (T)t;
    }

    private Cast() {
    }
}


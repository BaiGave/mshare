/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.spi.ThreadContextMap;
import org.apache.logging.log4j.util.ProviderUtil;

public final class ThreadContextMapFactory {
    public static void init() {
        ProviderUtil.getProvider().getThreadContextMapInstance();
    }

    private ThreadContextMapFactory() {
    }

    public static ThreadContextMap createThreadContextMap() {
        return ProviderUtil.getProvider().getThreadContextMapInstance();
    }
}


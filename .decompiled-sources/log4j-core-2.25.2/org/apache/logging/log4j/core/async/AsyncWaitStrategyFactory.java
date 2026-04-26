/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.WaitStrategy;

public interface AsyncWaitStrategyFactory {
    public WaitStrategy createWaitStrategy();
}


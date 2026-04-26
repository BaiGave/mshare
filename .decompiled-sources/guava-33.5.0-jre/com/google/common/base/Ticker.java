/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
public abstract class Ticker {
    private static final Ticker SYSTEM_TICKER = new Ticker(){

        @Override
        public long read() {
            return System.nanoTime();
        }
    };

    protected Ticker() {
    }

    public abstract long read();

    public static Ticker systemTicker() {
        return SYSTEM_TICKER;
    }
}


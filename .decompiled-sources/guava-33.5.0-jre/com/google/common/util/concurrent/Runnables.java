/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
public final class Runnables {
    private static final Runnable EMPTY_RUNNABLE = () -> {};

    public static Runnable doNothing() {
        return EMPTY_RUNNABLE;
    }

    private Runnables() {
    }
}


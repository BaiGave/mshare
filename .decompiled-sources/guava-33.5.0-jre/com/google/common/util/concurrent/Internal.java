/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import java.time.Duration;

@J2ktIncompatible
@GwtIncompatible
final class Internal {
    static long toNanosSaturated(Duration duration) {
        try {
            return duration.toNanos();
        }
        catch (ArithmeticException tooBig) {
            return duration.isNegative() ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
    }

    private Internal() {
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.IgnoreJRERequirement;
import java.time.Duration;

@J2ktIncompatible
@GwtIncompatible
final class Internal {
    @IgnoreJRERequirement
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


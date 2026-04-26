/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import java.util.concurrent.locks.LockSupport;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
final class OverflowAvoidingLockSupport {
    static final long MAX_NANOSECONDS_THRESHOLD = 2147483647999999999L;

    private OverflowAvoidingLockSupport() {
    }

    static void parkNanos(@Nullable Object blocker, long nanos) {
        LockSupport.parkNanos(blocker, Math.min(nanos, 2147483647999999999L));
    }
}


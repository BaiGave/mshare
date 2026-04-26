/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.hash;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

@GwtCompatible
final class SneakyThrows<T extends Throwable> {
    @CanIgnoreReturnValue
    static Error sneakyThrow(Throwable t) {
        throw super.throwIt(t);
    }

    private Error throwIt(Throwable t) throws T {
        throw t;
    }

    private SneakyThrows() {
    }
}


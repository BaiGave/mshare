/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.escape;

import com.google.common.annotations.GwtCompatible;
import java.util.Objects;

@GwtCompatible
final class Platform {
    private static final ThreadLocal<char[]> DEST_TL = new ThreadLocal<char[]>(){

        @Override
        protected char[] initialValue() {
            return new char[1024];
        }
    };

    private Platform() {
    }

    static char[] charBufferFromThreadLocal() {
        return Objects.requireNonNull(DEST_TL.get());
    }
}


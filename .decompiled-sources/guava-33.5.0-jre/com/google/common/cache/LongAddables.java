/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.cache;

import com.google.common.annotations.GwtCompatible;
import com.google.common.cache.LongAddable;
import java.util.concurrent.atomic.LongAdder;

@GwtCompatible
final class LongAddables {
    public static LongAddable create() {
        return new JavaUtilConcurrentLongAdder();
    }

    private LongAddables() {
    }

    private static final class JavaUtilConcurrentLongAdder
    extends LongAdder
    implements LongAddable {
        private JavaUtilConcurrentLongAdder() {
        }
    }
}


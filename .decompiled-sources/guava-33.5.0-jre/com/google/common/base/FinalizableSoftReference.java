/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.FinalizableReference;
import com.google.common.base.FinalizableReferenceQueue;
import java.lang.ref.SoftReference;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public abstract class FinalizableSoftReference<T>
extends SoftReference<T>
implements FinalizableReference {
    protected FinalizableSoftReference(@Nullable T referent, FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}


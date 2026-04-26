/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ParametricNullness;

@FunctionalInterface
@GwtCompatible
public interface AsyncFunction<I, O> {
    public ListenableFuture<O> apply(@ParametricNullness I var1) throws Exception;
}


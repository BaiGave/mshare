/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.util.concurrent;

import com.google.errorprone.annotations.DoNotMock;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import org.jspecify.annotations.NullMarked;

@DoNotMock(value="Use the methods in Futures (like immediateFuture) or SettableFuture")
@NullMarked
public interface ListenableFuture<V>
extends Future<V> {
    public void addListener(Runnable var1, Executor var2);
}


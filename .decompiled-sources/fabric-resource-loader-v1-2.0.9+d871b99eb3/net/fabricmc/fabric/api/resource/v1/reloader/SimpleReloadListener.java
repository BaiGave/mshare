/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.v1.reloader;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public abstract class SimpleReloadListener<T>
implements PreparableReloadListener {
    @Override
    public final CompletableFuture<Void> reload(PreparableReloadListener.SharedState state, Executor prepareExecutor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor applyExecutor) {
        CompletableFuture<Object> prepareStep = CompletableFuture.supplyAsync(() -> this.prepare(state), prepareExecutor);
        Objects.requireNonNull(preparationBarrier);
        return ((CompletableFuture)prepareStep.thenCompose(preparationBarrier::wait)).thenAcceptAsync(prepared -> this.apply(prepared, state), applyExecutor);
    }

    protected abstract T prepare(PreparableReloadListener.SharedState var1);

    protected abstract void apply(T var1, PreparableReloadListener.SharedState var2);
}


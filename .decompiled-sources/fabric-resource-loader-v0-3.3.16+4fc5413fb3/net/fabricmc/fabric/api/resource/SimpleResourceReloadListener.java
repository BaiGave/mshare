/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

@Deprecated
public interface SimpleResourceReloadListener<T>
extends IdentifiableResourceReloadListener {
    @Override
    default public CompletableFuture<Void> reload(PreparableReloadListener.SharedState store, Executor loadExecutor, PreparableReloadListener.PreparationBarrier helper, Executor applyExecutor) {
        return ((CompletableFuture)this.load(store.resourceManager(), loadExecutor).thenCompose(helper::wait)).thenCompose(o -> this.apply(o, store.resourceManager(), applyExecutor));
    }

    public CompletableFuture<T> load(ResourceManager var1, Executor var2);

    public CompletableFuture<Void> apply(T var1, ResourceManager var2, Executor var3);
}


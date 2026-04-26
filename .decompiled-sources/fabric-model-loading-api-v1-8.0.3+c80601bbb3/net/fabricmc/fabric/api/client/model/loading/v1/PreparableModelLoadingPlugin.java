/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnmodifiableView;

@FunctionalInterface
public interface PreparableModelLoadingPlugin<T> {
    public static <T> void register(DataLoader<T> loader, PreparableModelLoadingPlugin<T> plugin) {
        ModelLoadingPluginManager.registerPlugin(loader, plugin);
    }

    public static @UnmodifiableView List<Holder<?>> getAll() {
        return ModelLoadingPluginManager.PREPARABLE_PLUGINS_VIEW;
    }

    public void initialize(T var1, ModelLoadingPlugin.Context var2);

    @FunctionalInterface
    public static interface DataLoader<T> {
        public CompletableFuture<T> load(PreparableReloadListener.SharedState var1, Executor var2);
    }

    @ApiStatus.NonExtendable
    public static interface Holder<T> {
        public DataLoader<T> loader();

        public PreparableModelLoadingPlugin<T> plugin();
    }
}


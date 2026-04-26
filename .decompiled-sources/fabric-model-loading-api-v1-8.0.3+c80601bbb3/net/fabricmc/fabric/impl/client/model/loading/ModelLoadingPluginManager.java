/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.model.loading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.util.Util;
import org.jetbrains.annotations.UnmodifiableView;

public final class ModelLoadingPluginManager {
    private static final List<ModelLoadingPlugin> PLUGINS = new ArrayList<ModelLoadingPlugin>();
    private static final List<HolderImpl<?>> PREPARABLE_PLUGINS = new ArrayList();
    public static final @UnmodifiableView List<ModelLoadingPlugin> PLUGINS_VIEW = Collections.unmodifiableList(PLUGINS);
    public static final @UnmodifiableView List<PreparableModelLoadingPlugin.Holder<?>> PREPARABLE_PLUGINS_VIEW = Collections.unmodifiableList(PREPARABLE_PLUGINS);

    public static void registerPlugin(ModelLoadingPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin must not be null");
        PLUGINS.add(plugin);
    }

    public static <T> void registerPlugin(PreparableModelLoadingPlugin.DataLoader<T> loader, PreparableModelLoadingPlugin<T> plugin) {
        Objects.requireNonNull(loader, "data loader must not be null");
        Objects.requireNonNull(plugin, "plugin must not be null");
        PREPARABLE_PLUGINS.add(new HolderImpl<T>(loader, plugin));
    }

    public static CompletableFuture<List<ModelLoadingPlugin>> preparePlugins(PreparableReloadListener.SharedState resourceReloaderStore, Executor executor) {
        ArrayList<CompletableFuture<ModelLoadingPlugin>> futures = new ArrayList<CompletableFuture<ModelLoadingPlugin>>();
        for (ModelLoadingPlugin modelLoadingPlugin : PLUGINS) {
            futures.add(CompletableFuture.completedFuture(modelLoadingPlugin));
        }
        for (HolderImpl holderImpl : PREPARABLE_PLUGINS) {
            futures.add(ModelLoadingPluginManager.preparePlugin(holderImpl, resourceReloaderStore, executor));
        }
        return Util.sequence(futures);
    }

    private static <T> CompletableFuture<ModelLoadingPlugin> preparePlugin(HolderImpl<T> holder, PreparableReloadListener.SharedState resourceReloaderStore, Executor executor) {
        CompletableFuture dataFuture = holder.loader.load(resourceReloaderStore, executor);
        return dataFuture.thenApply(data -> pluginContext -> holder.plugin.initialize(data, pluginContext));
    }

    private ModelLoadingPluginManager() {
    }

    private record HolderImpl<T>(PreparableModelLoadingPlugin.DataLoader<T> loader, PreparableModelLoadingPlugin<T> plugin) implements PreparableModelLoadingPlugin.Holder<T>
    {
    }
}


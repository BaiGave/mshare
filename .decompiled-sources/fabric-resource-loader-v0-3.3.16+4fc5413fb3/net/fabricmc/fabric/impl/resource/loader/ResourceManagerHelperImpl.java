/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public class ResourceManagerHelperImpl
implements ResourceManagerHelper {
    private static final Map<PackType, ResourceManagerHelperImpl> registryMap = new HashMap<PackType, ResourceManagerHelperImpl>();
    private final ResourceLoader resourceLoader;

    private ResourceManagerHelperImpl(PackType type) {
        this.resourceLoader = ResourceLoader.get(type);
    }

    public static ResourceManagerHelperImpl get(PackType type) {
        return registryMap.computeIfAbsent(type, ResourceManagerHelperImpl::new);
    }

    @Override
    public void registerReloadListener(IdentifiableResourceReloadListener listener) {
        this.resourceLoader.registerReloadListener(listener.getFabricId(), listener);
        listener.getFabricDependencies().forEach(dependency -> this.resourceLoader.addListenerOrdering((Identifier)dependency, listener.getFabricId()));
    }

    @Override
    public void registerReloadListener(Identifier identifier, final Function<HolderLookup.Provider, IdentifiableResourceReloadListener> listenerFactory) {
        this.resourceLoader.registerReloadListener(identifier, new PreparableReloadListener(){
            {
                Objects.requireNonNull(this$0);
            }

            @Override
            public CompletableFuture<Void> reload(PreparableReloadListener.SharedState store, Executor prepareExecutor, PreparableReloadListener.PreparationBarrier reloadSynchronizer, Executor applyExecutor) {
                HolderLookup.Provider registries = store.get(ResourceLoader.REGISTRY_LOOKUP_KEY);
                PreparableReloadListener resourceReloader = (PreparableReloadListener)listenerFactory.apply(registries);
                return resourceReloader.reload(store, prepareExecutor, reloadSynchronizer, applyExecutor);
            }
        });
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.fabricmc.fabric.impl.resource.SetupMarkerResourceReloader;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.Nullable;

public final class DataResourceLoaderImpl
extends ResourceLoaderImpl
implements DataResourceLoader {
    public static final DataResourceLoaderImpl INSTANCE = new DataResourceLoaderImpl();
    private final Map<Identifier, Function<HolderLookup.Provider, PreparableReloadListener>> addedReloaderFactories = new LinkedHashMap<Identifier, Function<HolderLookup.Provider, PreparableReloadListener>>();

    private DataResourceLoaderImpl() {
        super(PackType.SERVER_DATA);
    }

    @Override
    protected boolean hasResourceReloader(Identifier id) {
        return super.hasResourceReloader(id) || this.addedReloaderFactories.containsKey(id);
    }

    @Override
    public void registerReloadListener(Identifier id, Function<HolderLookup.Provider, PreparableReloadListener> factory) {
        Objects.requireNonNull(id, "The reloader identifier should not be null.");
        Objects.requireNonNull(factory, "The reloader factory should not be null.");
        this.checkUniqueResourceReloader(id);
        for (Map.Entry<Identifier, Function<HolderLookup.Provider, PreparableReloadListener>> entry : this.addedReloaderFactories.entrySet()) {
            if (entry.getValue() != factory) continue;
            throw new IllegalStateException("Resource reloader factory with ID %s already in resource reloader factory set with ID %s!".formatted(id, entry.getKey()));
        }
        this.addedReloaderFactories.put(id, factory);
    }

    @Override
    protected Set<Map.Entry<Identifier, PreparableReloadListener>> collectReloadersToAdd(@Nullable SetupMarkerResourceReloader setupMarker) {
        if (setupMarker == null) {
            throw new IllegalStateException("The setup marker should not be null for data resource loading.");
        }
        HolderLookup.Provider registries = setupMarker.registries();
        Set<Map.Entry<Identifier, PreparableReloadListener>> reloadersToAdd = super.collectReloadersToAdd(setupMarker);
        for (Map.Entry<Identifier, Function<HolderLookup.Provider, PreparableReloadListener>> entry : this.addedReloaderFactories.entrySet()) {
            PreparableReloadListener reloader = entry.getValue().apply(registries);
            reloadersToAdd.add(Map.entry(entry.getKey(), reloader));
        }
        return reloadersToAdd;
    }
}


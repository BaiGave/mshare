/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync.trackers;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.impl.registry.sync.RemovableIdMapper;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class IdMapperTracker<V, OV>
implements RegistryEntryAddedCallback<V>,
RegistryIdRemapCallback<V> {
    private final String name;
    private final IdMapper<OV> mappers;
    private Map<Identifier, OV> removedMapperCache = new HashMap<Identifier, OV>();

    private IdMapperTracker(String name, IdMapper<OV> mappers) {
        this.name = name;
        this.mappers = mappers;
    }

    public static <V, OV> void register(Registry<V> registry, String name, IdMapper<OV> mappers) {
        IdMapperTracker<V, OV> updater = new IdMapperTracker<V, OV>(name, mappers);
        RegistryEntryAddedCallback.event(registry).register(updater);
        RegistryIdRemapCallback.event(registry).register(updater);
    }

    @Override
    public void onEntryAdded(int rawId, Identifier id, V object) {
        if (this.removedMapperCache.containsKey(id)) {
            this.mappers.addMapping(this.removedMapperCache.get(id), rawId);
        }
    }

    @Override
    public void onRemap(RegistryIdRemapCallback.RemapState<V> state) {
        ((RemovableIdMapper)((Object)this.mappers)).fabric_remapIds(state.getRawIdChangeMap());
    }
}


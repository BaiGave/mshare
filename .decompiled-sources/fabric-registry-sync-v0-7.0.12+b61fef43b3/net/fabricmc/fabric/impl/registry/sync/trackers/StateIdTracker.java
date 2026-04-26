/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync.trackers;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.fabricmc.fabric.impl.registry.sync.RemovableIdMapper;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StateIdTracker<T, S>
implements RegistryIdRemapCallback<T>,
RegistryEntryAddedCallback<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StateIdTracker.class);
    private static final Set<Identifier> TRACKED = new HashSet<Identifier>();
    private final Registry<T> registry;
    private final IdMapper<S> stateList;
    private final Function<T, Collection<S>> stateGetter;
    private int currentHighestId = 0;

    public static <T, S> void register(Registry<T> registry, IdMapper<S> stateList, Function<T, Collection<S>> stateGetter) {
        if (!TRACKED.add(registry.key().identifier())) {
            throw new IllegalStateException("Trying to register a tracker for registry " + String.valueOf(registry.key().identifier()) + " more than once!");
        }
        StateIdTracker<T, S> tracker = new StateIdTracker<T, S>(registry, stateList, stateGetter);
        RegistryEntryAddedCallback.event(registry).register(tracker);
        RegistryIdRemapCallback.event(registry).register(tracker);
    }

    private StateIdTracker(Registry<T> registry, IdMapper<S> stateList, Function<T, Collection<S>> stateGetter) {
        this.registry = registry;
        this.stateList = stateList;
        this.stateGetter = stateGetter;
        this.recalcHighestId();
    }

    @Override
    public void onEntryAdded(int rawId, Identifier id, T object) {
        if (rawId == this.currentHighestId + 1) {
            this.stateGetter.apply(object).forEach(this.stateList::add);
            this.currentHighestId = rawId;
        } else {
            LOGGER.debug("[fabric-registry-sync] Non-sequential RegistryEntryAddedCallback for " + object.getClass().getSimpleName() + " ID tracker (at " + String.valueOf(id) + "), forcing state map recalculation...");
            this.recalcStateMap();
        }
    }

    @Override
    public void onRemap(RegistryIdRemapCallback.RemapState<T> state) {
        this.recalcStateMap();
    }

    private void recalcStateMap() {
        ((RemovableIdMapper)((Object)this.stateList)).fabric_clear();
        Int2ObjectRBTreeMap sortedBlocks = new Int2ObjectRBTreeMap();
        this.currentHighestId = 0;
        this.registry.forEach(t -> {
            int rawId = this.registry.getId(t);
            this.currentHighestId = Math.max(this.currentHighestId, rawId);
            sortedBlocks.put(rawId, t);
        });
        for (Object b : sortedBlocks.values()) {
            this.stateGetter.apply(b).forEach(this.stateList::add);
        }
    }

    private void recalcHighestId() {
        this.currentHighestId = 0;
        for (Object object : this.registry) {
            this.currentHighestId = Math.max(this.currentHighestId, this.registry.getId(object));
        }
    }
}


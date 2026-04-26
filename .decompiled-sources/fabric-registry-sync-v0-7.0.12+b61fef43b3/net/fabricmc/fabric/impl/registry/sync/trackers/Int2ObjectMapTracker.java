/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync.trackers;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.lang.invoke.CallSite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Int2ObjectMapTracker<V, OV>
implements RegistryEntryAddedCallback<V>,
RegistryIdRemapCallback<V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Int2ObjectMapTracker.class);
    private final String name;
    private final Int2ObjectMap<OV> mappers;
    private Map<Identifier, OV> removedMapperCache = new HashMap<Identifier, OV>();

    private Int2ObjectMapTracker(String name, Int2ObjectMap<OV> mappers) {
        this.name = name;
        this.mappers = mappers;
    }

    public static <V, OV> void register(Registry<V> registry, String name, Int2ObjectMap<OV> mappers) {
        Int2ObjectMapTracker<V, OV> updater = new Int2ObjectMapTracker<V, OV>(name, mappers);
        RegistryEntryAddedCallback.event(registry).register(updater);
        RegistryIdRemapCallback.event(registry).register(updater);
    }

    @Override
    public void onEntryAdded(int rawId, Identifier id, V object) {
        if (this.removedMapperCache.containsKey(id)) {
            this.mappers.put(rawId, this.removedMapperCache.get(id));
        }
    }

    @Override
    public void onRemap(RegistryIdRemapCallback.RemapState<V> state) {
        Int2ObjectOpenHashMap<OV> oldMappers = new Int2ObjectOpenHashMap<OV>(this.mappers);
        Int2IntMap remapMap = state.getRawIdChangeMap();
        ArrayList<CallSite> errors = null;
        this.mappers.clear();
        IntIterator intIterator = oldMappers.keySet().iterator();
        while (intIterator.hasNext()) {
            int i = (Integer)intIterator.next();
            int newI = remapMap.getOrDefault(i, Integer.MIN_VALUE);
            if (newI >= 0) {
                if (this.mappers.containsKey(newI)) {
                    if (errors == null) {
                        errors = new ArrayList<CallSite>();
                    }
                    errors.add((CallSite)((Object)(" - Map contained two equal IDs " + newI + " (" + String.valueOf(state.getIdFromOld(i)) + "/" + i + " -> " + String.valueOf(state.getIdFromNew(newI)) + "/" + newI + ")!")));
                    continue;
                }
                this.mappers.put(newI, (OV)oldMappers.get(i));
                continue;
            }
            LOGGER.warn("[fabric-registry-sync] Int2ObjectMap " + this.name + " is dropping mapping for integer ID " + i + " (" + String.valueOf(state.getIdFromOld(i)) + ") - should not happen!");
            this.removedMapperCache.put(state.getIdFromOld(i), oldMappers.get(i));
        }
        if (errors != null) {
            throw new RuntimeException("Errors while remapping Int2ObjectMap " + this.name + " found:\n" + Joiner.on('\n').join(errors));
        }
    }
}


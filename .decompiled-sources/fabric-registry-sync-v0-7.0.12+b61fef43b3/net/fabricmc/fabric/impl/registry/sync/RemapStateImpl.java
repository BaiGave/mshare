/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class RemapStateImpl<T>
implements RegistryIdRemapCallback.RemapState<T> {
    private final Int2IntMap rawIdChangeMap;
    private final Int2ObjectMap<Identifier> oldIdMap;
    private final Int2ObjectMap<Identifier> newIdMap;

    public RemapStateImpl(Registry<T> registry, Int2ObjectMap<Identifier> oldIdMap, Int2IntMap rawIdChangeMap) {
        this.rawIdChangeMap = rawIdChangeMap;
        this.oldIdMap = oldIdMap;
        this.newIdMap = new Int2ObjectOpenHashMap<Identifier>();
        for (Int2IntMap.Entry entry : rawIdChangeMap.int2IntEntrySet()) {
            Identifier id = registry.getKey(registry.byId(entry.getIntValue()));
            this.newIdMap.put(entry.getIntValue(), id);
        }
    }

    @Override
    public Int2IntMap getRawIdChangeMap() {
        return this.rawIdChangeMap;
    }

    @Override
    public Identifier getIdFromOld(int oldRawId) {
        return (Identifier)this.oldIdMap.get(oldRawId);
    }

    @Override
    public Identifier getIdFromNew(int newRawId) {
        return (Identifier)this.newIdMap.get(newRawId);
    }
}


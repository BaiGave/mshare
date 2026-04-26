/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.resource.v1.DataResourceStore;

public final class DataResourceStoreImpl
implements DataResourceStore.Mutable {
    private final Map<DataResourceStore.Key<?>, Object> store = new IdentityHashMap();

    @Override
    public <T> void put(DataResourceStore.Key<T> key, T data) {
        this.store.put(key, data);
    }

    @Override
    public <T> T getOrThrow(DataResourceStore.Key<T> key) {
        return (T)Objects.requireNonNull(this.store.get(key));
    }
}


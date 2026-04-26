/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.lookup.custom;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiProviderMap;
import org.jspecify.annotations.Nullable;

public final class ApiProviderHashMap<K, V>
implements ApiProviderMap<K, V> {
    private volatile Map<K, V> lookups = new Reference2ReferenceOpenHashMap();

    @Override
    public @Nullable V get(K key) {
        Objects.requireNonNull(key, "Key may not be null.");
        return this.lookups.get(key);
    }

    @Override
    public synchronized V putIfAbsent(K key, V provider) {
        Objects.requireNonNull(key, "Key may not be null.");
        Objects.requireNonNull(provider, "Provider may not be null.");
        Reference2ReferenceOpenHashMap<K, V> lookupsCopy = new Reference2ReferenceOpenHashMap<K, V>(this.lookups);
        V result = lookupsCopy.putIfAbsent(key, provider);
        this.lookups = lookupsCopy;
        return result;
    }
}


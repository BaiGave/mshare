/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.lookup.v1.custom;

import net.fabricmc.fabric.impl.lookup.custom.ApiProviderHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface ApiProviderMap<K, V> {
    public static <K, V> ApiProviderMap<K, V> create() {
        return new ApiProviderHashMap();
    }

    public @Nullable V get(K var1);

    public V putIfAbsent(K var1, V var2);
}


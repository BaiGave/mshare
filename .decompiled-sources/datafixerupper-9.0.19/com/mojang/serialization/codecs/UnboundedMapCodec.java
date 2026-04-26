/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.BaseMapCodec;
import java.util.Map;

public record UnboundedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements BaseMapCodec<K, V>,
Codec<Map<K, V>>
{
    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap((? super R map) -> this.decode(ops, (Object)map)).map((? super R r) -> Pair.of(r, input));
    }

    @Override
    public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
        return this.encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    @Override
    public String toString() {
        return "UnboundedMapCodec[" + String.valueOf(this.keyCodec) + " -> " + String.valueOf(this.elementCodec) + "]";
    }
}


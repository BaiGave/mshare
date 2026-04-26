/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface BaseMapCodec<K, V> {
    public Codec<K> keyCodec();

    public Codec<V> elementCodec();

    default public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
        Object2ObjectArrayMap read = new Object2ObjectArrayMap();
        Stream.Builder failed = Stream.builder();
        DataResult result = input.entries().reduce(DataResult.success(Unit.INSTANCE, Lifecycle.stable()), (r, pair) -> {
            Object existingValue;
            DataResult value;
            DataResult key = this.keyCodec().parse(ops, pair.getFirst());
            DataResult<Pair> entryResult = key.apply2stable(Pair::of, value = this.elementCodec().parse(ops, pair.getSecond()));
            Optional<Pair> entry = entryResult.resultOrPartial();
            if (entry.isPresent() && (existingValue = read.putIfAbsent(entry.get().getFirst(), entry.get().getSecond())) != null) {
                failed.add(pair);
                return r.apply2stable((u, p) -> u, DataResult.error(() -> "Duplicate entry for key: '" + String.valueOf(((Pair)entry.get()).getFirst()) + "'"));
            }
            if (entryResult.isError()) {
                failed.add(pair);
            }
            return r.apply2stable((u, p) -> u, entryResult);
        }, (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2));
        ImmutableMap elements = ImmutableMap.copyOf(read);
        Object errors = ops.createMap(failed.build());
        return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + String.valueOf(errors));
    }

    default public <T> RecordBuilder<T> encode(Map<K, V> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        for (Map.Entry<K, V> entry : input.entrySet()) {
            prefix.add(this.keyCodec().encodeStart(ops, entry.getKey()), this.elementCodec().encodeStart(ops, entry.getValue()));
        }
        return prefix;
    }
}


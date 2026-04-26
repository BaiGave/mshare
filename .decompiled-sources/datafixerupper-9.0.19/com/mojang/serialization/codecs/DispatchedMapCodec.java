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
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public record DispatchedMapCodec<K, V>(Codec<K> keyCodec, Function<K, Codec<? extends V>> valueCodecFunction) implements Codec<Map<K, V>>
{
    @Override
    public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
        RecordBuilder<T> mapBuilder = ops.mapBuilder();
        for (Map.Entry<K, V> entry : input.entrySet()) {
            mapBuilder.add(this.keyCodec.encodeStart(ops, entry.getKey()), this.encodeValue(this.valueCodecFunction.apply(entry.getKey()), entry.getValue(), ops));
        }
        return mapBuilder.build(prefix);
    }

    private <T, V2 extends V> DataResult<T> encodeValue(Codec<V2> codec, V input, DynamicOps<T> ops) {
        return codec.encodeStart(ops, input);
    }

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input).flatMap((? super R map) -> {
            Object2ObjectArrayMap entries = new Object2ObjectArrayMap();
            Stream.Builder failed = Stream.builder();
            DataResult finalResult = map.entries().reduce(DataResult.success(Unit.INSTANCE, Lifecycle.stable()), (result, entry) -> this.parseEntry((DataResult<Unit>)result, ops, (Pair)entry, entries, failed), (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2));
            Pair pair = Pair.of(ImmutableMap.copyOf(entries), input);
            Object errors = ops.createMap(failed.build());
            return finalResult.map((? super R ignored) -> pair).setPartial(pair).mapError(error -> error + " missed input: " + String.valueOf(errors));
        });
    }

    private <T> DataResult<Unit> parseEntry(DataResult<Unit> result, DynamicOps<T> ops, Pair<T, T> input, Map<K, V> entries, Stream.Builder<Pair<T, T>> failed) {
        Object value;
        Object key;
        DataResult valueResult;
        DataResult keyResult = this.keyCodec.parse(ops, input.getFirst());
        DataResult<Pair> entryResult = keyResult.apply2stable(Pair::of, valueResult = keyResult.map(this.valueCodecFunction).flatMap((? super R valueCodec) -> valueCodec.parse(ops, input.getSecond()).map(Function.identity())));
        Optional<Pair> entry = entryResult.resultOrPartial();
        if (entry.isPresent() && entries.putIfAbsent(key = entry.get().getFirst(), value = entry.get().getSecond()) != null) {
            failed.add(input);
            return result.apply2stable((u, p) -> u, DataResult.error(() -> "Duplicate entry for key: '" + String.valueOf(key) + "'"));
        }
        if (entryResult.isError()) {
            failed.add(input);
        }
        return result.apply2stable((u, p) -> u, entryResult);
    }
}


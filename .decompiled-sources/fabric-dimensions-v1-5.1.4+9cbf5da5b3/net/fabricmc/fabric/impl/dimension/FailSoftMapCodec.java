/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.dimension;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.BaseMapCodec;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record FailSoftMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements BaseMapCodec<K, V>,
Codec<Map<K, V>>
{
    private static final Logger LOGGER = LoggerFactory.getLogger("FailSoftMapCodec");

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap((? super R map) -> this.decode(ops, (MapLike)map)).map((? super R r) -> Pair.of(r, input));
    }

    @Override
    public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
        return this.encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    @Override
    public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        input.entries().forEach(pair -> {
            try {
                DataResult k = this.keyCodec().parse(ops, pair.getFirst());
                DataResult v = this.elementCodec().parse(ops, pair.getSecond());
                Optional optionalK = k.result();
                Optional optionalV = v.result();
                if (optionalK.isEmpty()) {
                    LOGGER.error("Failed to decode key {} from {}  {}", k, pair, k.resultOrPartial());
                }
                if (optionalV.isEmpty()) {
                    LOGGER.error("Failed to decode value {} from {}  {}", k, pair, v.resultOrPartial());
                }
                if (optionalK.isPresent() && optionalV.isPresent()) {
                    builder.put(optionalK.get(), optionalV.get());
                }
            }
            catch (Throwable e) {
                LOGGER.error("Decoding {}", pair, (Object)e);
            }
        });
        ImmutableMap elements = builder.build();
        return DataResult.success(elements);
    }

    @Override
    public String toString() {
        return "FailSoftMapCodec[" + String.valueOf(this.keyCodec) + " -> " + String.valueOf(this.elementCodec) + "]";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Function;
import java.util.stream.Stream;

public class KeyDispatchCodec<K, V>
extends MapCodec<V> {
    private static final String COMPRESSED_VALUE_KEY = "value";
    private final MapCodec<K> keyCodec;
    private final Function<? super V, ? extends DataResult<? extends K>> type;
    private final Function<? super K, ? extends DataResult<? extends MapDecoder<? extends V>>> decoder;
    private final Function<? super V, ? extends DataResult<? extends MapEncoder<V>>> encoder;

    protected KeyDispatchCodec(MapCodec<K> keyCodec, Function<? super V, ? extends DataResult<? extends K>> type, Function<? super K, ? extends DataResult<? extends MapDecoder<? extends V>>> decoder, Function<? super V, ? extends DataResult<? extends MapEncoder<V>>> encoder) {
        this.keyCodec = keyCodec;
        this.type = type;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    public KeyDispatchCodec(MapCodec<K> keyCodec, Function<? super V, ? extends DataResult<? extends K>> type, Function<? super K, ? extends DataResult<? extends MapCodec<? extends V>>> codec) {
        this(keyCodec, type, codec, v -> KeyDispatchCodec.getCodec(type, codec, v));
    }

    @Override
    public <T> DataResult<V> decode(DynamicOps<T> ops, MapLike<T> input) {
        return this.keyCodec.decode(ops, input).flatMap((? super R type) -> this.decoder.apply(type).flatMap((? super R elementDecoder) -> {
            if (ops.compressMaps()) {
                Object value = input.get(ops.createString(COMPRESSED_VALUE_KEY));
                if (value == null) {
                    return DataResult.error(() -> "Input does not have a \"value\" entry: " + String.valueOf(input));
                }
                return elementDecoder.decoder().parse(ops, value).map(Function.identity());
            }
            return elementDecoder.decode(ops, input).map(Function.identity());
        }));
    }

    @Override
    public <T> RecordBuilder<T> encode(V input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        DataResult<MapEncoder<V>> encoderResult = this.encoder.apply(input);
        DataResult<K> typeResult = this.type.apply(input);
        RecordBuilder<T> builder = prefix.withErrorsFrom(encoderResult).withErrorsFrom(typeResult);
        if (encoderResult.isError() || typeResult.isError()) {
            return builder;
        }
        MapEncoder<V> elementEncoder = encoderResult.getOrThrow();
        K type = typeResult.getOrThrow();
        if (ops.compressMaps()) {
            return this.keyCodec.encode(type, ops, builder).add(COMPRESSED_VALUE_KEY, elementEncoder.encoder().encodeStart(ops, input));
        }
        RecordBuilder<T> encodedContents = elementEncoder.encode(input, ops, builder);
        return this.keyCodec.encode(type, ops, encodedContents);
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.concat(this.keyCodec.keys(ops), Stream.of(ops.createString(COMPRESSED_VALUE_KEY)));
    }

    private static <K, V> DataResult<? extends MapEncoder<V>> getCodec(Function<? super V, ? extends DataResult<? extends K>> type, Function<? super K, ? extends DataResult<? extends MapEncoder<? extends V>>> encoder, V input) {
        return type.apply(input).flatMap((? super R key) -> ((DataResult)encoder.apply((Object)key)).map(Function.identity())).map((? super R c) -> c);
    }

    public String toString() {
        return "KeyDispatchCodec[" + this.keyCodec.toString() + " " + String.valueOf(this.type) + " " + String.valueOf(this.decoder) + "]";
    }
}


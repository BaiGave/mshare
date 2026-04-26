/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public record EitherCodec<F, S>(Codec<F> first, Codec<S> second) implements Codec<Either<F, S>>
{
    @Override
    public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> ops, T input) {
        DataResult firstRead = this.first.decode(ops, input).map((? super R vo) -> vo.mapFirst(Either::left));
        if (firstRead.isSuccess()) {
            return firstRead;
        }
        DataResult secondRead = this.second.decode(ops, input).map((? super R vo) -> vo.mapFirst(Either::right));
        if (secondRead.isSuccess()) {
            return secondRead;
        }
        if (firstRead.hasResultOrPartial()) {
            return firstRead;
        }
        if (secondRead.hasResultOrPartial()) {
            return secondRead;
        }
        return DataResult.error(() -> "Failed to parse either. First: " + firstRead.error().orElseThrow().message() + "; Second: " + secondRead.error().orElseThrow().message());
    }

    @Override
    public <T> DataResult<T> encode(Either<F, S> input, DynamicOps<T> ops, T prefix) {
        return input.map(value1 -> this.first.encode(value1, ops, prefix), value2 -> this.second.encode(value2, ops, prefix));
    }
}


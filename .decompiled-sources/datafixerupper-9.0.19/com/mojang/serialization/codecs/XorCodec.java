/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;

public record XorCodec<F, S>(Codec<F> first, Codec<S> second) implements Codec<Either<F, S>>
{
    @Override
    public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> ops, T input) {
        DataResult<Pair<Either<F, Pair>, T>> firstRead = this.first.decode(ops, input).map((? super R vo) -> vo.mapFirst(Either::left));
        DataResult<Pair<Either<F, S>, T>> secondRead = this.second.decode(ops, input).map((? super R vo) -> vo.mapFirst(Either::right));
        Optional<Pair> firstResult = firstRead.result();
        Optional<Pair> secondResult = secondRead.result();
        if (firstResult.isPresent() && secondResult.isPresent()) {
            return DataResult.error(() -> "Both alternatives read successfully, can not pick the correct one; first: " + String.valueOf(firstResult.get()) + " second: " + String.valueOf(secondResult.get()), firstResult.get());
        }
        if (firstResult.isPresent()) {
            return firstRead;
        }
        if (secondResult.isPresent()) {
            return secondRead;
        }
        return firstRead.apply2((f, s) -> s, secondRead);
    }

    @Override
    public <T> DataResult<T> encode(Either<F, S> input, DynamicOps<T> ops, T prefix) {
        return input.map(value1 -> this.first.encode(value1, ops, prefix), value2 -> this.second.encode(value2, ops, prefix));
    }
}


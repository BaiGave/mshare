/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record ListCodec<E>(Codec<E> elementCodec, int minSize, int maxSize) implements Codec<List<E>>
{
    private <R> DataResult<R> createTooShortError(int size) {
        return DataResult.error(() -> "List is too short: " + size + ", expected range [" + this.minSize + "-" + this.maxSize + "]");
    }

    private <R> DataResult<R> createTooLongError(int size) {
        return DataResult.error(() -> "List is too long: " + size + ", expected range [" + this.minSize + "-" + this.maxSize + "]");
    }

    @Override
    public <T> DataResult<T> encode(List<E> input, DynamicOps<T> ops, T prefix) {
        if (input.size() < this.minSize) {
            return this.createTooShortError(input.size());
        }
        if (input.size() > this.maxSize) {
            return this.createTooLongError(input.size());
        }
        ListBuilder<T> builder = ops.listBuilder();
        for (E element : input) {
            builder.add(this.elementCodec.encodeStart(ops, element));
        }
        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<List<E>, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap((? super R stream) -> {
            DecoderState decoder = new DecoderState(ops);
            stream.accept(decoder::accept);
            return decoder.build();
        });
    }

    @Override
    public String toString() {
        return "ListCodec[" + String.valueOf(this.elementCodec) + "]";
    }

    private class DecoderState<T> {
        private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());
        private final DynamicOps<T> ops;
        private final List<E> elements = new ArrayList();
        private final Stream.Builder<T> failed = Stream.builder();
        private DataResult<Unit> result = INITIAL_RESULT;
        private int totalCount;

        private DecoderState(DynamicOps<T> ops) {
            this.ops = ops;
        }

        public void accept(T value) {
            ++this.totalCount;
            if (this.elements.size() >= ListCodec.this.maxSize) {
                this.failed.add(value);
                return;
            }
            DataResult elementResult = ListCodec.this.elementCodec.decode(this.ops, value);
            elementResult.error().ifPresent(error -> this.failed.add(value));
            elementResult.resultOrPartial().ifPresent(pair -> this.elements.add(pair.getFirst()));
            this.result = this.result.apply2stable((result, element) -> result, elementResult);
        }

        public DataResult<Pair<List<E>, T>> build() {
            if (this.elements.size() < ListCodec.this.minSize) {
                return ListCodec.this.createTooShortError(this.elements.size());
            }
            T errors = this.ops.createList(this.failed.build());
            Pair pair = Pair.of(List.copyOf(this.elements), errors);
            if (this.totalCount > ListCodec.this.maxSize) {
                this.result = ListCodec.this.createTooLongError(this.totalCount);
            }
            return this.result.map(ignored -> pair).setPartial(pair);
        }
    }
}


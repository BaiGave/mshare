/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class OptionalFieldCodec<A>
extends MapCodec<Optional<A>> {
    private final String name;
    private final Codec<A> elementCodec;
    private final boolean lenient;

    public OptionalFieldCodec(String name, Codec<A> elementCodec, boolean lenient) {
        this.name = name;
        this.elementCodec = elementCodec;
        this.lenient = lenient;
    }

    @Override
    public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
        T value = input.get(this.name);
        if (value == null) {
            return DataResult.success(Optional.empty());
        }
        DataResult parsed = this.elementCodec.parse(ops, value);
        if (parsed.isError() && this.lenient) {
            return DataResult.success(Optional.empty());
        }
        return parsed.map(Optional::of).setPartial(parsed.resultOrPartial());
    }

    @Override
    public <T> RecordBuilder<T> encode(Optional<A> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        if (input.isPresent()) {
            return prefix.add(this.name, this.elementCodec.encodeStart(ops, input.get()));
        }
        return prefix;
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(ops.createString(this.name));
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        OptionalFieldCodec that = (OptionalFieldCodec)o;
        return Objects.equals(this.name, that.name) && Objects.equals(this.elementCodec, that.elementCodec) && this.lenient == that.lenient;
    }

    public int hashCode() {
        return Objects.hash(this.name, this.elementCodec, this.lenient);
    }

    public String toString() {
        return "OptionalFieldCodec[" + this.name + ": " + String.valueOf(this.elementCodec) + "]";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.serialization.codecs;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.RecordBuilder;
import java.util.Objects;
import java.util.stream.Stream;

public class FieldEncoder<A>
extends MapEncoder.Implementation<A> {
    private final String name;
    private final Encoder<A> elementCodec;

    public FieldEncoder(String name, Encoder<A> elementCodec) {
        this.name = name;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        DataResult<T> encodedContents = this.elementCodec.encodeStart(ops, input);
        return prefix.add(this.name, encodedContents);
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
        FieldEncoder that = (FieldEncoder)o;
        return Objects.equals(this.name, that.name) && Objects.equals(this.elementCodec, that.elementCodec);
    }

    public int hashCode() {
        return Objects.hash(this.name, this.elementCodec);
    }

    public String toString() {
        return "FieldEncoder[" + this.name + ": " + String.valueOf(this.elementCodec) + "]";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import java.util.function.UnaryOperator;

abstract class AbstractListBuilder<T, B>
implements ListBuilder<T> {
    private final DynamicOps<T> ops;
    protected DataResult<B> builder = DataResult.success(this.initBuilder(), Lifecycle.stable());

    protected AbstractListBuilder(DynamicOps<T> ops) {
        this.ops = ops;
    }

    @Override
    public DynamicOps<T> ops() {
        return this.ops;
    }

    protected abstract B initBuilder();

    protected abstract B append(B var1, T var2);

    protected abstract DataResult<T> build(B var1, T var2);

    @Override
    public ListBuilder<T> add(T value) {
        this.builder = this.builder.map(b -> this.append(b, value));
        return this;
    }

    @Override
    public ListBuilder<T> add(DataResult<T> value) {
        this.builder = this.builder.apply2stable(this::append, value);
        return this;
    }

    @Override
    public ListBuilder<T> withErrorsFrom(DataResult<?> result) {
        this.builder = this.builder.flatMap(r -> result.map(v -> r));
        return this;
    }

    @Override
    public ListBuilder<T> mapError(UnaryOperator<String> onError) {
        this.builder = this.builder.mapError(onError);
        return this;
    }

    @Override
    public DataResult<T> build(T prefix) {
        DataResult result = this.builder.flatMap(b -> this.build(b, prefix));
        this.builder = DataResult.success(this.initBuilder(), Lifecycle.stable());
        return result;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.logging.log4j.util.LazyUtil;

public interface Lazy<T>
extends Supplier<T> {
    public T value();

    @Override
    default public T get() {
        return this.value();
    }

    default public <R> Lazy<R> map(Function<? super T, ? extends R> function) {
        return Lazy.lazy(() -> function.apply((T)this.value()));
    }

    public boolean isInitialized();

    public void set(T var1);

    public static <T> Lazy<T> lazy(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return new LazyUtil.SafeLazy<T>(supplier);
    }

    public static <T> Lazy<T> value(T value) {
        return new LazyUtil.Constant<T>(value);
    }

    public static <T> Lazy<T> weak(T value) {
        return new LazyUtil.WeakConstant<T>(value);
    }

    public static <T> Lazy<T> pure(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return new LazyUtil.PureLazy<T>(supplier);
    }
}


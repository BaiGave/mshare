/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.function;

import java.util.Objects;
import java.util.function.Function;

public final class Functions {
    public static <T, R> R apply(Function<T, R> function, T object) {
        return function != null ? (R)function.apply(object) : null;
    }

    public static <T, R> R applyNonNull(T value, Function<? super T, ? extends R> mapper) {
        return value != null ? (R)Objects.requireNonNull(mapper, "mapper").apply((T)value) : null;
    }

    public static <T, U, R> R applyNonNull(T value1, Function<? super T, ? extends U> mapper1, Function<? super U, ? extends R> mapper2) {
        return Functions.applyNonNull(Functions.applyNonNull(value1, mapper1), mapper2);
    }

    public static <T, U, V, R> R applyNonNull(T value1, Function<? super T, ? extends U> mapper1, Function<? super U, ? extends V> mapper2, Function<? super V, ? extends R> mapper3) {
        return Functions.applyNonNull(Functions.applyNonNull(Functions.applyNonNull(value1, mapper1), mapper2), mapper3);
    }

    public static <T, R> Function<T, R> function(Function<T, R> function) {
        return function;
    }

    private Functions() {
    }
}


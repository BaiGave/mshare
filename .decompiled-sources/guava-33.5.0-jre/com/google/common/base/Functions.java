/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Function;
import com.google.common.base.NullnessCasts;
import com.google.common.base.ParametricNullness;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public final class Functions {
    private Functions() {
    }

    public static Function<Object, String> toStringFunction() {
        return ToStringFunction.INSTANCE;
    }

    public static <E> Function<E, E> identity() {
        return IdentityFunction.INSTANCE;
    }

    public static <K, V> Function<K, V> forMap(Map<K, V> map) {
        return new FunctionForMapNoDefault<K, V>(map);
    }

    public static <K, V> Function<K, V> forMap(Map<K, ? extends V> map, @ParametricNullness V defaultValue) {
        return new ForMapWithDefault<K, V>(map, defaultValue);
    }

    public static <A, B, C> Function<A, C> compose(Function<B, C> g, Function<A, ? extends B> f) {
        return new FunctionComposition<A, B, C>(g, f);
    }

    public static <T> Function<T, Boolean> forPredicate(Predicate<T> predicate) {
        return new PredicateFunction(predicate);
    }

    public static <E> Function<@Nullable Object, E> constant(@ParametricNullness E value) {
        return new ConstantFunction<E>(value);
    }

    public static <F, T> Function<F, T> forSupplier(Supplier<T> supplier) {
        return new SupplierFunction(supplier);
    }

    private static enum ToStringFunction implements Function<Object, String>
    {
        INSTANCE;


        @Override
        public String apply(Object o) {
            Preconditions.checkNotNull(o);
            return o.toString();
        }

        public String toString() {
            return "Functions.toStringFunction()";
        }
    }

    private static enum IdentityFunction implements Function<Object, Object>
    {
        INSTANCE;


        @Override
        public @Nullable Object apply(@Nullable Object o) {
            return o;
        }

        public String toString() {
            return "Functions.identity()";
        }
    }

    private static final class FunctionForMapNoDefault<K, V>
    implements Function<K, V>,
    Serializable {
        final Map<K, V> map;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        FunctionForMapNoDefault(Map<K, V> map) {
            this.map = Preconditions.checkNotNull(map);
        }

        @Override
        @ParametricNullness
        public V apply(@ParametricNullness K key) {
            V result = this.map.get(key);
            Preconditions.checkArgument(result != null || this.map.containsKey(key), "Key '%s' not present in map", key);
            return NullnessCasts.uncheckedCastNullableTToT(result);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o instanceof FunctionForMapNoDefault) {
                FunctionForMapNoDefault that = (FunctionForMapNoDefault)o;
                return this.map.equals(that.map);
            }
            return false;
        }

        public int hashCode() {
            return this.map.hashCode();
        }

        public String toString() {
            return "Functions.forMap(" + this.map + ")";
        }
    }

    private static final class ForMapWithDefault<K, V>
    implements Function<K, V>,
    Serializable {
        final Map<K, ? extends V> map;
        @ParametricNullness
        final V defaultValue;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        ForMapWithDefault(Map<K, ? extends V> map, @ParametricNullness V defaultValue) {
            this.map = Preconditions.checkNotNull(map);
            this.defaultValue = defaultValue;
        }

        @Override
        @ParametricNullness
        public V apply(@ParametricNullness K key) {
            V result = this.map.get(key);
            return result != null || this.map.containsKey(key) ? NullnessCasts.uncheckedCastNullableTToT(result) : this.defaultValue;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o instanceof ForMapWithDefault) {
                ForMapWithDefault that = (ForMapWithDefault)o;
                return this.map.equals(that.map) && Objects.equals(this.defaultValue, that.defaultValue);
            }
            return false;
        }

        public int hashCode() {
            return Objects.hash(this.map, this.defaultValue);
        }

        public String toString() {
            return "Functions.forMap(" + this.map + ", defaultValue=" + this.defaultValue + ")";
        }
    }

    private static final class FunctionComposition<A, B, C>
    implements Function<A, C>,
    Serializable {
        private final Function<B, C> g;
        private final Function<A, ? extends B> f;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        FunctionComposition(Function<B, C> g, Function<A, ? extends B> f) {
            this.g = Preconditions.checkNotNull(g);
            this.f = Preconditions.checkNotNull(f);
        }

        @Override
        @ParametricNullness
        public C apply(@ParametricNullness A a) {
            return this.g.apply(this.f.apply(a));
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof FunctionComposition) {
                FunctionComposition that = (FunctionComposition)obj;
                return this.f.equals(that.f) && this.g.equals(that.g);
            }
            return false;
        }

        public int hashCode() {
            return this.f.hashCode() ^ this.g.hashCode();
        }

        public String toString() {
            return this.g + "(" + this.f + ")";
        }
    }

    private static final class PredicateFunction<T>
    implements Function<T, Boolean>,
    Serializable {
        private final Predicate<T> predicate;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        private PredicateFunction(Predicate<T> predicate) {
            this.predicate = Preconditions.checkNotNull(predicate);
        }

        @Override
        public Boolean apply(@ParametricNullness T t) {
            return this.predicate.apply(t);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof PredicateFunction) {
                PredicateFunction that = (PredicateFunction)obj;
                return this.predicate.equals(that.predicate);
            }
            return false;
        }

        public int hashCode() {
            return this.predicate.hashCode();
        }

        public String toString() {
            return "Functions.forPredicate(" + this.predicate + ")";
        }
    }

    private static final class ConstantFunction<E>
    implements Function<Object, E>,
    Serializable {
        @ParametricNullness
        private final E value;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        ConstantFunction(@ParametricNullness E value) {
            this.value = value;
        }

        @Override
        @ParametricNullness
        public E apply(@Nullable Object from) {
            return this.value;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof ConstantFunction) {
                ConstantFunction that = (ConstantFunction)obj;
                return Objects.equals(this.value, that.value);
            }
            return false;
        }

        public int hashCode() {
            return this.value == null ? 0 : this.value.hashCode();
        }

        public String toString() {
            return "Functions.constant(" + this.value + ")";
        }
    }

    private static final class SupplierFunction<F, T>
    implements Function<F, T>,
    Serializable {
        private final Supplier<T> supplier;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        private SupplierFunction(Supplier<T> supplier) {
            this.supplier = Preconditions.checkNotNull(supplier);
        }

        @Override
        @ParametricNullness
        public T apply(@ParametricNullness F input) {
            return this.supplier.get();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof SupplierFunction) {
                SupplierFunction that = (SupplierFunction)obj;
                return this.supplier.equals(that.supplier);
            }
            return false;
        }

        public int hashCode() {
            return this.supplier.hashCode();
        }

        public String toString() {
            return "Functions.forSupplier(" + this.supplier + ")";
        }
    }
}


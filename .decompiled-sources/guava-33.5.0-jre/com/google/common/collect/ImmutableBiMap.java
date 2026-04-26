/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.CollectCollectors;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.RegularImmutableBiMap;
import com.google.common.collect.SingletonImmutableBiMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ImmutableBiMap<K, V>
extends ImmutableMap<K, V>
implements BiMap<K, V> {
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = -889275714L;

    public static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toImmutableBiMap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction) {
        return CollectCollectors.toImmutableBiMap(keyFunction, valueFunction);
    }

    public static <K, V> ImmutableBiMap<K, V> of() {
        return RegularImmutableBiMap.EMPTY;
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1) {
        return new SingletonImmutableBiMap<K, V>(k1, v1);
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2));
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2), ImmutableBiMap.entryOf(k3, v3));
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2), ImmutableBiMap.entryOf(k3, v3), ImmutableBiMap.entryOf(k4, v4));
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2), ImmutableBiMap.entryOf(k3, v3), ImmutableBiMap.entryOf(k4, v4), ImmutableBiMap.entryOf(k5, v5));
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2), ImmutableBiMap.entryOf(k3, v3), ImmutableBiMap.entryOf(k4, v4), ImmutableBiMap.entryOf(k5, v5), ImmutableBiMap.entryOf(k6, v6));
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2), ImmutableBiMap.entryOf(k3, v3), ImmutableBiMap.entryOf(k4, v4), ImmutableBiMap.entryOf(k5, v5), ImmutableBiMap.entryOf(k6, v6), ImmutableBiMap.entryOf(k7, v7));
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2), ImmutableBiMap.entryOf(k3, v3), ImmutableBiMap.entryOf(k4, v4), ImmutableBiMap.entryOf(k5, v5), ImmutableBiMap.entryOf(k6, v6), ImmutableBiMap.entryOf(k7, v7), ImmutableBiMap.entryOf(k8, v8));
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2), ImmutableBiMap.entryOf(k3, v3), ImmutableBiMap.entryOf(k4, v4), ImmutableBiMap.entryOf(k5, v5), ImmutableBiMap.entryOf(k6, v6), ImmutableBiMap.entryOf(k7, v7), ImmutableBiMap.entryOf(k8, v8), ImmutableBiMap.entryOf(k9, v9));
    }

    public static <K, V> ImmutableBiMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return RegularImmutableBiMap.fromEntries(ImmutableBiMap.entryOf(k1, v1), ImmutableBiMap.entryOf(k2, v2), ImmutableBiMap.entryOf(k3, v3), ImmutableBiMap.entryOf(k4, v4), ImmutableBiMap.entryOf(k5, v5), ImmutableBiMap.entryOf(k6, v6), ImmutableBiMap.entryOf(k7, v7), ImmutableBiMap.entryOf(k8, v8), ImmutableBiMap.entryOf(k9, v9), ImmutableBiMap.entryOf(k10, v10));
    }

    @SafeVarargs
    public static <K, V> ImmutableBiMap<K, V> ofEntries(Map.Entry<? extends K, ? extends V> ... entries) {
        Map.Entry<? extends K, ? extends V>[] entries2 = entries;
        return RegularImmutableBiMap.fromEntries(entries2);
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder();
    }

    public static <K, V> Builder<K, V> builderWithExpectedSize(int expectedSize) {
        CollectPreconditions.checkNonnegative(expectedSize, "expectedSize");
        return new Builder(expectedSize);
    }

    public static <K, V> ImmutableBiMap<K, V> copyOf(Map<? extends K, ? extends V> map) {
        ImmutableBiMap bimap;
        if (map instanceof ImmutableBiMap && !(bimap = (ImmutableBiMap)map).isPartialView()) {
            return bimap;
        }
        return ImmutableBiMap.copyOf(map.entrySet());
    }

    public static <K, V> ImmutableBiMap<K, V> copyOf(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        Map.Entry[] entryArray = Iterables.toArray(entries, EMPTY_ENTRY_ARRAY);
        switch (entryArray.length) {
            case 0: {
                return ImmutableBiMap.of();
            }
            case 1: {
                Map.Entry entry = entryArray[0];
                return ImmutableBiMap.of(entry.getKey(), entry.getValue());
            }
        }
        return RegularImmutableBiMap.fromEntries(entryArray);
    }

    ImmutableBiMap() {
    }

    @Override
    public abstract ImmutableBiMap<V, K> inverse();

    @Override
    public ImmutableSet<V> values() {
        return ((ImmutableMap)((Object)this.inverse())).keySet();
    }

    @Override
    final ImmutableSet<V> createValues() {
        throw new AssertionError((Object)"should never be called");
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public final @Nullable V forcePut(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    @J2ktIncompatible
    Object writeReplace() {
        return new SerializedForm(this);
    }

    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use SerializedForm");
    }

    @Deprecated
    @DoNotCall(value="Use toImmutableBiMap")
    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @DoNotCall(value="Use toImmutableBiMap")
    public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction, BinaryOperator<V> mergeFunction) {
        throw new UnsupportedOperationException();
    }

    public static final class Builder<K, V>
    extends ImmutableMap.Builder<K, V> {
        public Builder() {
        }

        Builder(int size) {
            super(size);
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> put(K key, V value) {
            super.put(key, value);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> put(Map.Entry<? extends K, ? extends V> entry) {
            super.put(entry);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Map<? extends K, ? extends V> map) {
            super.putAll(map);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
            super.putAll(entries);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        public Builder<K, V> orderEntriesByValue(Comparator<? super V> valueComparator) {
            super.orderEntriesByValue(valueComparator);
            return this;
        }

        @Override
        @CanIgnoreReturnValue
        Builder<K, V> combine(ImmutableMap.Builder<K, V> builder) {
            super.combine(builder);
            return this;
        }

        @Override
        public ImmutableBiMap<K, V> build() {
            return this.buildOrThrow();
        }

        @Override
        public ImmutableBiMap<K, V> buildOrThrow() {
            switch (this.size) {
                case 0: {
                    return ImmutableBiMap.of();
                }
                case 1: {
                    Map.Entry onlyEntry = Objects.requireNonNull(this.entries[0]);
                    return ImmutableBiMap.of(onlyEntry.getKey(), onlyEntry.getValue());
                }
            }
            if (this.valueComparator != null) {
                if (this.entriesUsed) {
                    this.entries = Arrays.copyOf(this.entries, this.size);
                }
                Arrays.sort(this.entries, 0, this.size, Ordering.from(this.valueComparator).onResultOf(Maps.valueFunction()));
            }
            this.entriesUsed = true;
            return RegularImmutableBiMap.fromEntryArray(this.size, this.entries);
        }

        @Override
        @Deprecated
        @DoNotCall
        public ImmutableBiMap<K, V> buildKeepingLast() {
            throw new UnsupportedOperationException("Not supported for bimaps");
        }

        @Override
        @VisibleForTesting
        ImmutableBiMap<K, V> buildJdkBacked() {
            Preconditions.checkState(this.valueComparator == null, "buildJdkBacked is for tests only, doesn't support orderEntriesByValue");
            switch (this.size) {
                case 0: {
                    return ImmutableBiMap.of();
                }
                case 1: {
                    Map.Entry onlyEntry = Objects.requireNonNull(this.entries[0]);
                    return ImmutableBiMap.of(onlyEntry.getKey(), onlyEntry.getValue());
                }
            }
            this.entriesUsed = true;
            return RegularImmutableBiMap.fromEntryArray(this.size, this.entries);
        }
    }

    @J2ktIncompatible
    private static final class SerializedForm<K, V>
    extends ImmutableMap.SerializedForm<K, V> {
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        SerializedForm(ImmutableBiMap<K, V> bimap) {
            super(bimap);
        }

        @Override
        Builder<K, V> makeBuilder(int size) {
            return new Builder(size);
        }
    }
}


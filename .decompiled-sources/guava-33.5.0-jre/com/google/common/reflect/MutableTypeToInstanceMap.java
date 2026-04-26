/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.reflect;

import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingMapEntry;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Iterators;
import com.google.common.reflect.ParametricNullness;
import com.google.common.reflect.TypeToInstanceMap;
import com.google.common.reflect.TypeToken;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MutableTypeToInstanceMap<B>
extends ForwardingMap<TypeToken<? extends B>, B>
implements TypeToInstanceMap<B> {
    private final Map<TypeToken<? extends @NonNull B>, B> backingMap = new HashMap<TypeToken<? extends B>, B>();

    @Override
    public <T extends B> @Nullable T getInstance(Class<T> type) {
        return this.trustedGet(TypeToken.of(type));
    }

    @Override
    public <T extends B> @Nullable T getInstance(TypeToken<T> type) {
        return this.trustedGet(type.rejectTypeVariables());
    }

    @Override
    @CanIgnoreReturnValue
    public <T extends B> @Nullable T putInstance(Class<@NonNull T> type, @ParametricNullness T value) {
        return this.trustedPut(TypeToken.of(type), value);
    }

    @Override
    @CanIgnoreReturnValue
    public <T extends B> @Nullable T putInstance(TypeToken<@NonNull T> type, @ParametricNullness T value) {
        return this.trustedPut(type.rejectTypeVariables(), value);
    }

    @Override
    @Deprecated
    @CanIgnoreReturnValue
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public @Nullable B put(TypeToken<? extends @NonNull B> key, @ParametricNullness B value) {
        throw new UnsupportedOperationException("Please use putInstance() instead.");
    }

    @Override
    @Deprecated
    @DoNotCall(value="Always throws UnsupportedOperationException")
    public void putAll(Map<? extends TypeToken<? extends @NonNull B>, ? extends B> map) {
        throw new UnsupportedOperationException("Please use putInstance() instead.");
    }

    @Override
    public Set<Map.Entry<TypeToken<? extends @NonNull B>, B>> entrySet() {
        return UnmodifiableEntry.transformEntries(super.entrySet());
    }

    @Override
    protected Map<TypeToken<? extends @NonNull B>, B> delegate() {
        return this.backingMap;
    }

    private <T extends B> @Nullable T trustedPut(TypeToken<@NonNull T> type, @ParametricNullness T value) {
        return (T)this.backingMap.put(type, value);
    }

    private <T extends B> @Nullable T trustedGet(TypeToken<T> type) {
        return (T)this.backingMap.get(type);
    }

    private static final class UnmodifiableEntry<K, V>
    extends ForwardingMapEntry<K, V> {
        private final Map.Entry<K, V> delegate;

        static <K, V> Set<Map.Entry<K, V>> transformEntries(final Set<Map.Entry<K, V>> entries) {
            return new ForwardingSet<Map.Entry<K, V>>(){

                @Override
                protected Set<Map.Entry<K, V>> delegate() {
                    return entries;
                }

                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    return UnmodifiableEntry.transformEntries(super.iterator());
                }

                @Override
                public Object[] toArray() {
                    Object[] result = this.standardToArray();
                    return result;
                }

                @Override
                public <T> T[] toArray(T[] array) {
                    return this.standardToArray(array);
                }
            };
        }

        private static <K, V> Iterator<Map.Entry<K, V>> transformEntries(Iterator<Map.Entry<K, V>> entries) {
            return Iterators.transform(entries, UnmodifiableEntry::new);
        }

        private UnmodifiableEntry(Map.Entry<K, V> delegate) {
            this.delegate = Preconditions.checkNotNull(delegate);
        }

        @Override
        protected Map.Entry<K, V> delegate() {
            return this.delegate;
        }

        @Override
        @ParametricNullness
        public V setValue(@ParametricNullness V value) {
            throw new UnsupportedOperationException();
        }
    }
}


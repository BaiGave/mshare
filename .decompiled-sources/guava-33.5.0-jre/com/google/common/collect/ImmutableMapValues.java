/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.CollectSpliterators;
import com.google.common.collect.ImmutableAsList;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import java.io.Serializable;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class ImmutableMapValues<K, V>
extends ImmutableCollection<V> {
    private final ImmutableMap<K, V> map;

    ImmutableMapValues(ImmutableMap<K, V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public UnmodifiableIterator<V> iterator() {
        return new UnmodifiableIterator<V>(){
            final UnmodifiableIterator<Map.Entry<K, V>> entryItr;
            {
                this.entryItr = ((ImmutableSet)ImmutableMapValues.this.map.entrySet()).iterator();
            }

            @Override
            public boolean hasNext() {
                return this.entryItr.hasNext();
            }

            @Override
            public V next() {
                return ((Map.Entry)this.entryItr.next()).getValue();
            }
        };
    }

    @Override
    public Spliterator<V> spliterator() {
        return CollectSpliterators.map(((ImmutableCollection)((Object)this.map.entrySet())).spliterator(), Map.Entry::getValue);
    }

    @Override
    public boolean contains(@Nullable Object object) {
        return object != null && Iterators.contains(this.iterator(), object);
    }

    @Override
    boolean isPartialView() {
        return true;
    }

    @Override
    public ImmutableList<V> asList() {
        final ImmutableList entryList = ((ImmutableCollection)((Object)this.map.entrySet())).asList();
        return new ImmutableAsList<V>(this){
            final /* synthetic */ ImmutableMapValues this$0;
            {
                this.this$0 = this$0;
            }

            @Override
            public V get(int index) {
                return ((Map.Entry)entryList.get(index)).getValue();
            }

            @Override
            ImmutableCollection<V> delegateCollection() {
                return this.this$0;
            }

            @Override
            @J2ktIncompatible
            @GwtIncompatible
            Object writeReplace() {
                return super.writeReplace();
            }
        };
    }

    @Override
    @GwtIncompatible
    public void forEach(Consumer<? super V> action) {
        Preconditions.checkNotNull(action);
        this.map.forEach((? super K k, ? super V v) -> action.accept((Object)v));
    }

    @Override
    @J2ktIncompatible
    @GwtIncompatible
    Object writeReplace() {
        return super.writeReplace();
    }

    @GwtIncompatible
    @J2ktIncompatible
    private static final class SerializedForm<V>
    implements Serializable {
        final ImmutableMap<?, V> map;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        SerializedForm(ImmutableMap<?, V> map) {
            this.map = map;
        }

        Object readResolve() {
            return this.map.values();
        }
    }
}


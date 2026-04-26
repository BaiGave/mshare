/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.RegularImmutableAsList;
import com.google.common.collect.UnmodifiableIterator;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class ImmutableMapEntrySet<K, V>
extends ImmutableSet.CachingAsList<Map.Entry<K, V>> {
    ImmutableMapEntrySet() {
    }

    abstract ImmutableMap<K, V> map();

    @Override
    public int size() {
        return this.map().size();
    }

    @Override
    public boolean contains(@Nullable Object object) {
        if (object instanceof Map.Entry) {
            Map.Entry entry = (Map.Entry)object;
            V value = this.map().get(entry.getKey());
            return value != null && value.equals(entry.getValue());
        }
        return false;
    }

    @Override
    boolean isPartialView() {
        return this.map().isPartialView();
    }

    @Override
    @GwtIncompatible
    boolean isHashCodeFast() {
        return this.map().isHashCodeFast();
    }

    @Override
    public int hashCode() {
        return this.map().hashCode();
    }

    @Override
    @GwtIncompatible
    @J2ktIncompatible
    Object writeReplace() {
        return new EntrySetSerializedForm<K, V>(this.map());
    }

    @GwtIncompatible
    @J2ktIncompatible
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Use EntrySetSerializedForm");
    }

    @GwtIncompatible
    @J2ktIncompatible
    private static final class EntrySetSerializedForm<K, V>
    implements Serializable {
        final ImmutableMap<K, V> map;
        @GwtIncompatible
        @J2ktIncompatible
        private static final long serialVersionUID = 0L;

        EntrySetSerializedForm(ImmutableMap<K, V> map) {
            this.map = map;
        }

        Object readResolve() {
            return this.map.entrySet();
        }
    }

    static final class RegularEntrySet<K, V>
    extends ImmutableMapEntrySet<K, V> {
        private final transient ImmutableMap<K, V> map;
        private final transient ImmutableList<Map.Entry<K, V>> entries;

        RegularEntrySet(ImmutableMap<K, V> map, Map.Entry<K, V>[] entries) {
            this(map, ImmutableList.asImmutableList(entries));
        }

        RegularEntrySet(ImmutableMap<K, V> map, ImmutableList<Map.Entry<K, V>> entries) {
            this.map = map;
            this.entries = entries;
        }

        @Override
        ImmutableMap<K, V> map() {
            return this.map;
        }

        @Override
        @GwtIncompatible(value="not used in GWT")
        int copyIntoArray(@Nullable Object[] dst, int offset) {
            return this.entries.copyIntoArray(dst, offset);
        }

        @Override
        public UnmodifiableIterator<Map.Entry<K, V>> iterator() {
            return this.entries.iterator();
        }

        @Override
        public Spliterator<Map.Entry<K, V>> spliterator() {
            return this.entries.spliterator();
        }

        @Override
        public void forEach(Consumer<? super Map.Entry<K, V>> action) {
            this.entries.forEach((Consumer<Map.Entry<K, V>>)action);
        }

        @Override
        ImmutableList<Map.Entry<K, V>> createAsList() {
            return new RegularImmutableAsList<Map.Entry<K, V>>(this, this.entries);
        }

        @Override
        @J2ktIncompatible
        @GwtIncompatible
        Object writeReplace() {
            return super.writeReplace();
        }
    }
}


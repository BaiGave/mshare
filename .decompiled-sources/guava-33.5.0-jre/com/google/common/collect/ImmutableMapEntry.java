/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.CollectPreconditions;
import com.google.common.collect.ParametricNullness;
import java.util.AbstractMap;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
class ImmutableMapEntry<K, V>
extends AbstractMap.SimpleImmutableEntry<K, V> {
    static <K, V> ImmutableMapEntry<K, V>[] createEntryArray(int size) {
        return new ImmutableMapEntry[size];
    }

    ImmutableMapEntry(K key, V value) {
        super(key, value);
        CollectPreconditions.checkEntryNotNull(key, value);
    }

    @Override
    @ParametricNullness
    public final K getKey() {
        return super.getKey();
    }

    @Override
    @ParametricNullness
    public final V getValue() {
        return super.getValue();
    }

    @Override
    @ParametricNullness
    public final V setValue(@ParametricNullness V value) {
        return super.setValue(value);
    }

    @Nullable ImmutableMapEntry<K, V> getNextInKeyBucket() {
        return null;
    }

    @Nullable ImmutableMapEntry<K, V> getNextInValueBucket() {
        return null;
    }

    boolean isReusable() {
        return true;
    }

    static final class NonTerminalImmutableBiMapEntry<K, V>
    extends NonTerminalImmutableMapEntry<K, V> {
        private final transient @Nullable ImmutableMapEntry<K, V> nextInValueBucket;

        NonTerminalImmutableBiMapEntry(K key, V value, @Nullable ImmutableMapEntry<K, V> nextInKeyBucket, @Nullable ImmutableMapEntry<K, V> nextInValueBucket) {
            super(key, value, nextInKeyBucket);
            this.nextInValueBucket = nextInValueBucket;
        }

        @Override
        @Nullable ImmutableMapEntry<K, V> getNextInValueBucket() {
            return this.nextInValueBucket;
        }
    }

    static class NonTerminalImmutableMapEntry<K, V>
    extends ImmutableMapEntry<K, V> {
        private final transient @Nullable ImmutableMapEntry<K, V> nextInKeyBucket;

        NonTerminalImmutableMapEntry(K key, V value, @Nullable ImmutableMapEntry<K, V> nextInKeyBucket) {
            super(key, value);
            this.nextInKeyBucket = nextInKeyBucket;
        }

        @Override
        final @Nullable ImmutableMapEntry<K, V> getNextInKeyBucket() {
            return this.nextInKeyBucket;
        }

        @Override
        final boolean isReusable() {
            return false;
        }
    }
}


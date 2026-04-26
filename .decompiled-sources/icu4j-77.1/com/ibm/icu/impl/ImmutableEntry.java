/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.UnicodeMap;
import java.util.Map;

public class ImmutableEntry<K, V>
implements Map.Entry<K, V> {
    final K k;
    final V v;

    ImmutableEntry(K key, V value) {
        this.k = key;
        this.v = value;
    }

    @Override
    public K getKey() {
        return this.k;
    }

    @Override
    public V getValue() {
        return this.v;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        try {
            Map.Entry e = (Map.Entry)o;
            return UnicodeMap.areEqual(e.getKey(), this.k) && UnicodeMap.areEqual(e.getValue(), this.v);
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (this.k == null ? 0 : this.k.hashCode()) ^ (this.v == null ? 0 : this.v.hashCode());
    }

    public String toString() {
        return this.k + "=" + this.v;
    }
}


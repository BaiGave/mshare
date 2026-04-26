/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ParametricNullness;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible
abstract class AbstractMapEntry<K, V>
implements Map.Entry<K, V> {
    AbstractMapEntry() {
    }

    @Override
    @ParametricNullness
    public abstract K getKey();

    @Override
    @ParametricNullness
    public abstract V getValue();

    @Override
    @ParametricNullness
    public V setValue(@ParametricNullness V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof Map.Entry) {
            Map.Entry that = (Map.Entry)object;
            return Objects.equals(this.getKey(), that.getKey()) && Objects.equals(this.getValue(), that.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        K k = this.getKey();
        V v = this.getValue();
        return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
    }

    public String toString() {
        return this.getKey() + "=" + this.getValue();
    }
}


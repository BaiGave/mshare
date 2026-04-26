/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public abstract class ForwardingMapEntry<K, V>
extends ForwardingObject
implements Map.Entry<K, V> {
    protected ForwardingMapEntry() {
    }

    @Override
    protected abstract Map.Entry<K, V> delegate();

    @Override
    @ParametricNullness
    public K getKey() {
        return this.delegate().getKey();
    }

    @Override
    @ParametricNullness
    public V getValue() {
        return this.delegate().getValue();
    }

    @Override
    @ParametricNullness
    @CanIgnoreReturnValue
    public V setValue(@ParametricNullness V value) {
        return this.delegate().setValue(value);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return this.delegate().equals(object);
    }

    @Override
    public int hashCode() {
        return this.delegate().hashCode();
    }

    protected boolean standardEquals(@Nullable Object object) {
        if (object instanceof Map.Entry) {
            Map.Entry that = (Map.Entry)object;
            return Objects.equals(this.getKey(), that.getKey()) && Objects.equals(this.getValue(), that.getValue());
        }
        return false;
    }

    protected int standardHashCode() {
        K k = this.getKey();
        V v = this.getValue();
        return (k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode());
    }

    protected String standardToString() {
        return this.getKey() + "=" + this.getValue();
    }
}


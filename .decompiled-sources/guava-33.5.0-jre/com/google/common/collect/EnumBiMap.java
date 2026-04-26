/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractBiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.Platform;
import com.google.common.collect.Serialization;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

@GwtCompatible
@J2ktIncompatible
public final class EnumBiMap<K extends Enum<K>, V extends Enum<V>>
extends AbstractBiMap<K, V> {
    transient Class<K> keyTypeOrObjectUnderJ2cl;
    transient Class<V> valueTypeOrObjectUnderJ2cl;
    @GwtIncompatible
    @J2ktIncompatible
    private static final long serialVersionUID = 0L;

    public static <K extends Enum<K>, V extends Enum<V>> EnumBiMap<K, V> create(Class<K> keyType, Class<V> valueType) {
        return new EnumBiMap<K, V>(keyType, valueType);
    }

    public static <K extends Enum<K>, V extends Enum<V>> EnumBiMap<K, V> create(Map<K, V> map) {
        EnumBiMap<K, V> bimap = EnumBiMap.create(EnumBiMap.inferKeyTypeOrObjectUnderJ2cl(map), EnumBiMap.inferValueTypeOrObjectUnderJ2cl(map));
        bimap.putAll((Map)map);
        return bimap;
    }

    private EnumBiMap(Class<K> keyTypeOrObjectUnderJ2cl, Class<V> valueTypeOrObjectUnderJ2cl) {
        super(new EnumMap(keyTypeOrObjectUnderJ2cl), new EnumMap(valueTypeOrObjectUnderJ2cl));
        this.keyTypeOrObjectUnderJ2cl = keyTypeOrObjectUnderJ2cl;
        this.valueTypeOrObjectUnderJ2cl = valueTypeOrObjectUnderJ2cl;
    }

    static <K extends Enum<K>> Class<K> inferKeyTypeOrObjectUnderJ2cl(Map<K, ?> map) {
        if (map instanceof EnumBiMap) {
            return ((EnumBiMap)map).keyTypeOrObjectUnderJ2cl;
        }
        if (map instanceof EnumHashBiMap) {
            return ((EnumHashBiMap)map).keyTypeOrObjectUnderJ2cl;
        }
        Preconditions.checkArgument(!map.isEmpty());
        return Platform.getDeclaringClassOrObjectForJ2cl((Enum)map.keySet().iterator().next());
    }

    private static <V extends Enum<V>> Class<V> inferValueTypeOrObjectUnderJ2cl(Map<?, V> map) {
        if (map instanceof EnumBiMap) {
            return ((EnumBiMap)map).valueTypeOrObjectUnderJ2cl;
        }
        Preconditions.checkArgument(!map.isEmpty());
        return Platform.getDeclaringClassOrObjectForJ2cl((Enum)map.values().iterator().next());
    }

    @GwtIncompatible
    public Class<K> keyType() {
        return this.keyTypeOrObjectUnderJ2cl;
    }

    @GwtIncompatible
    public Class<V> valueType() {
        return this.valueTypeOrObjectUnderJ2cl;
    }

    @Override
    K checkKey(K key) {
        return (K)((Enum)Preconditions.checkNotNull(key));
    }

    @Override
    V checkValue(V value) {
        return (V)((Enum)Preconditions.checkNotNull(value));
    }

    @GwtIncompatible
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(this.keyTypeOrObjectUnderJ2cl);
        stream.writeObject(this.valueTypeOrObjectUnderJ2cl);
        Serialization.writeMap(this, stream);
    }

    @GwtIncompatible
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.keyTypeOrObjectUnderJ2cl = (Class)Objects.requireNonNull(stream.readObject());
        this.valueTypeOrObjectUnderJ2cl = (Class)Objects.requireNonNull(stream.readObject());
        this.setDelegates(new EnumMap(this.keyTypeOrObjectUnderJ2cl), new EnumMap(this.valueTypeOrObjectUnderJ2cl));
        Serialization.populateMap(this, stream);
    }
}


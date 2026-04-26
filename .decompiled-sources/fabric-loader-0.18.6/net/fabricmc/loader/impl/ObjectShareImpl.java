/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.fabricmc.loader.api.ObjectShare;

final class ObjectShareImpl
implements ObjectShare {
    private final Map<String, Object> values = new HashMap<String, Object>();
    private final Map<String, List<BiConsumer<String, Object>>> pendingMap = new HashMap<String, List<BiConsumer<String, Object>>>();

    ObjectShareImpl() {
    }

    @Override
    public synchronized Object get(String key) {
        ObjectShareImpl.validateKey(key);
        return this.values.get(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object put(String key, Object value) {
        List<BiConsumer<String, Object>> pending;
        ObjectShareImpl.validateKey(key);
        Objects.requireNonNull(value, "null value");
        ObjectShareImpl objectShareImpl = this;
        synchronized (objectShareImpl) {
            Object prev = this.values.put(key, value);
            if (prev != null) {
                return prev;
            }
            pending = this.pendingMap.remove(key);
        }
        if (pending != null) {
            ObjectShareImpl.invokePending(key, value, pending);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public Object putIfAbsent(String key, Object value) {
        List<BiConsumer<String, Object>> pending;
        ObjectShareImpl.validateKey(key);
        Objects.requireNonNull(value, "null value");
        ObjectShareImpl objectShareImpl = this;
        synchronized (objectShareImpl) {
            Object prev = this.values.putIfAbsent(key, value);
            if (prev != null) {
                return prev;
            }
            pending = this.pendingMap.remove(key);
        }
        if (pending != null) {
            ObjectShareImpl.invokePending(key, value, pending);
        }
        return null;
    }

    @Override
    public synchronized Object remove(String key) {
        ObjectShareImpl.validateKey(key);
        return this.values.remove(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void whenAvailable(String key, BiConsumer<String, Object> consumer) {
        Object value;
        ObjectShareImpl.validateKey(key);
        ObjectShareImpl objectShareImpl = this;
        synchronized (objectShareImpl) {
            value = this.values.get(key);
            if (value == null) {
                this.pendingMap.computeIfAbsent(key, ignore -> new ArrayList()).add(consumer);
                return;
            }
        }
        consumer.accept(key, value);
    }

    private static void validateKey(String key) {
        Objects.requireNonNull(key, "null key");
        int pos = key.indexOf(58);
        if (pos <= 0 || pos >= key.length() - 1) {
            throw new IllegalArgumentException("invalid key, must be modid:subkey");
        }
    }

    private static void invokePending(String key, Object value, List<BiConsumer<String, Object>> pending) {
        for (BiConsumer<String, Object> consumer : pending) {
            consumer.accept(key, value);
        }
    }
}


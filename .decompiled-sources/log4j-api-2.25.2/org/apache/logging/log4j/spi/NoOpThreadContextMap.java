/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.spi;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.spi.ThreadContextMap;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NoOpThreadContextMap
implements ThreadContextMap {
    public static final ThreadContextMap INSTANCE = new NoOpThreadContextMap();

    @Override
    public void clear() {
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public @Nullable String get(String key) {
        return null;
    }

    @Override
    public Map<String, String> getCopy() {
        return new HashMap<String, String>();
    }

    @Override
    public @Nullable Map<String, String> getImmutableMapOrNull() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void put(String key, String value) {
    }

    @Override
    public void remove(String key) {
    }
}


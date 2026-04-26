/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.v1;

public interface DataResourceStore {
    public <T> T getOrThrow(Key<T> var1);

    public static interface Mutable
    extends DataResourceStore {
        public <T> void put(Key<T> var1, T var2);
    }

    public static final class Key<T> {
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.debug.v1;

@FunctionalInterface
public interface DebugValueFactory<D, T> {
    public T create(D var1);
}


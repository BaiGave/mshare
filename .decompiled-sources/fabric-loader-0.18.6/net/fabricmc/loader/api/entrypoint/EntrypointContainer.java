/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api.entrypoint;

import net.fabricmc.loader.api.ModContainer;

public interface EntrypointContainer<T> {
    public T getEntrypoint();

    public ModContainer getProvider();

    default public String getDefinition() {
        return "";
    }
}


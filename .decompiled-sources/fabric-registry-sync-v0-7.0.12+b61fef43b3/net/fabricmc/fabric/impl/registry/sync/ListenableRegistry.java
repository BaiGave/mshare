/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.minecraft.core.Registry;

public interface ListenableRegistry<T> {
    public Event<RegistryEntryAddedCallback<T>> fabric_getAddObjectEvent();

    public Event<RegistryIdRemapCallback<T>> fabric_getRemapEvent();

    public static <T> ListenableRegistry<T> get(Registry<T> registry) {
        if (!(registry instanceof ListenableRegistry)) {
            throw new IllegalArgumentException("Unsupported registry: " + String.valueOf(registry.key().identifier()));
        }
        return (ListenableRegistry)((Object)registry);
    }
}


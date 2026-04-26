/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.registry;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.registry.sync.ListenableRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface RegistryEntryAddedCallback<T> {
    public void onEntryAdded(int var1, Identifier var2, T var3);

    public static <T> Event<RegistryEntryAddedCallback<T>> event(Registry<T> registry) {
        return ListenableRegistry.get(registry).fabric_getAddObjectEvent();
    }

    public static <T> void allEntries(Registry<T> registry, Consumer<Holder.Reference<T>> consumer) {
        RegistryEntryAddedCallback.event(registry).register((rawId, id, object) -> consumer.accept(registry.get(id).orElseThrow()));
        registry.listElements().toList().forEach(consumer);
    }
}


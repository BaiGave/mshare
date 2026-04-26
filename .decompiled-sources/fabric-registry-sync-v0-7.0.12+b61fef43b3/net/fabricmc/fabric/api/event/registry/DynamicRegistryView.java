/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.registry;

import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface DynamicRegistryView {
    public RegistryAccess asRegistryAccess();

    public Stream<Registry<?>> stream();

    public <T> Optional<Registry<T>> getOptional(ResourceKey<? extends Registry<? extends T>> var1);

    public <T> void registerEntryAdded(ResourceKey<? extends Registry<? extends T>> var1, RegistryEntryAddedCallback<T> var2);
}


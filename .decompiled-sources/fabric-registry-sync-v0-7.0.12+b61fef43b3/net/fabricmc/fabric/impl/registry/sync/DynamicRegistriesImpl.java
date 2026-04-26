/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryValidator;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Unmodifiable;

public final class DynamicRegistriesImpl {
    private static final List<RegistryDataLoader.RegistryData<?>> DYNAMIC_REGISTRIES = new ArrayList(RegistryDataLoader.WORLDGEN_REGISTRIES);
    public static final Set<ResourceKey<?>> FABRIC_DYNAMIC_REGISTRY_KEYS = new HashSet();
    public static final Set<ResourceKey<? extends Registry<?>>> DYNAMIC_REGISTRY_KEYS = new HashSet();
    public static final Set<ResourceKey<? extends Registry<?>>> SKIP_EMPTY_SYNC_REGISTRIES = new HashSet();

    private DynamicRegistriesImpl() {
    }

    public static @Unmodifiable List<RegistryDataLoader.RegistryData<?>> getDynamicRegistries() {
        return List.copyOf(DYNAMIC_REGISTRIES);
    }

    public static <T> RegistryDataLoader.RegistryData<T> register(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec) {
        Objects.requireNonNull(key, "Registry key cannot be null");
        Objects.requireNonNull(serverCodec, "Server codec cannot be null");
        if (!DYNAMIC_REGISTRY_KEYS.add(key)) {
            throw new IllegalArgumentException("Dynamic registry " + String.valueOf(key) + " has already been registered!");
        }
        RegistryDataLoader.RegistryData<T> entry = new RegistryDataLoader.RegistryData<T>(key, serverCodec, RegistryValidator.none());
        DYNAMIC_REGISTRIES.add(entry);
        FABRIC_DYNAMIC_REGISTRY_KEYS.add(key);
        return entry;
    }

    public static <T> void addSyncedRegistry(ResourceKey<? extends Registry<T>> key, Codec<T> clientCodec, DynamicRegistries.SyncOption ... options) {
        Objects.requireNonNull(key, "Registry key cannot be null");
        Objects.requireNonNull(clientCodec, "Client codec cannot be null");
        Objects.requireNonNull(options, "Options cannot be null");
        if (!(RegistryDataLoader.SYNCHRONIZED_REGISTRIES instanceof ArrayList)) {
            RegistryDataLoader.SYNCHRONIZED_REGISTRIES = new ArrayList(RegistryDataLoader.SYNCHRONIZED_REGISTRIES);
        }
        RegistryDataLoader.SYNCHRONIZED_REGISTRIES.add(new RegistryDataLoader.RegistryData<T>(key, clientCodec, RegistryValidator.none()));
        if (!(RegistrySynchronization.NETWORKABLE_REGISTRIES instanceof HashSet)) {
            RegistrySynchronization.NETWORKABLE_REGISTRIES = new HashSet(RegistrySynchronization.NETWORKABLE_REGISTRIES);
        }
        RegistrySynchronization.NETWORKABLE_REGISTRIES.add(key);
        for (DynamicRegistries.SyncOption option : options) {
            if (option != DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY) continue;
            SKIP_EMPTY_SYNC_REGISTRIES.add(key);
        }
    }

    static {
        for (RegistryDataLoader.RegistryData<?> vanillaEntry : RegistryDataLoader.WORLDGEN_REGISTRIES) {
            DYNAMIC_REGISTRY_KEYS.add(vanillaEntry.key());
        }
    }
}


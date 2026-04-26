/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.registry;

import com.mojang.serialization.Codec;
import java.util.List;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Unmodifiable;

public final class DynamicRegistries {
    private DynamicRegistries() {
    }

    public static @Unmodifiable List<RegistryDataLoader.RegistryData<?>> getDynamicRegistries() {
        return DynamicRegistriesImpl.getDynamicRegistries();
    }

    public static <T> void register(ResourceKey<? extends Registry<T>> key, Codec<T> codec) {
        DynamicRegistriesImpl.register(key, codec);
    }

    public static <T> void registerSynced(ResourceKey<? extends Registry<T>> key, Codec<T> codec, SyncOption ... options) {
        DynamicRegistries.registerSynced(key, codec, codec, options);
    }

    public static <T> void registerSynced(ResourceKey<? extends Registry<T>> key, Codec<T> serverCodec, Codec<T> clientCodec, SyncOption ... options) {
        DynamicRegistriesImpl.register(key, serverCodec);
        DynamicRegistriesImpl.addSyncedRegistry(key, clientCodec, options);
    }

    public static enum SyncOption {
        SKIP_WHEN_EMPTY;

    }
}


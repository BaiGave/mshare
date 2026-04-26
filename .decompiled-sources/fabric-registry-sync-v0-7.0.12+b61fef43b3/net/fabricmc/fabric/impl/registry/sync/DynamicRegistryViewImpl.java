/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

public final class DynamicRegistryViewImpl
implements DynamicRegistryView {
    private final Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries;

    public DynamicRegistryViewImpl(Map<ResourceKey<? extends Registry<?>>, Registry<?>> registries) {
        this.registries = registries;
    }

    @Override
    public RegistryAccess asRegistryAccess() {
        return new RegistryAccess.Frozen(this){
            final /* synthetic */ DynamicRegistryViewImpl this$0;
            {
                DynamicRegistryViewImpl dynamicRegistryViewImpl = this$0;
                Objects.requireNonNull(dynamicRegistryViewImpl);
                this.this$0 = dynamicRegistryViewImpl;
            }

            public <T> Optional<Registry<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                return Optional.ofNullable(this.this$0.registries.get(key));
            }

            @Override
            public Stream<RegistryAccess.RegistryEntry<?>> registries() {
                return this.this$0.stream().map(this::entry);
            }

            private <T> RegistryAccess.RegistryEntry<T> entry(Registry<T> registry) {
                return new RegistryAccess.RegistryEntry<T>(registry.key(), registry);
            }

            @Override
            public RegistryAccess.Frozen freeze() {
                return this;
            }
        };
    }

    @Override
    public Stream<Registry<?>> stream() {
        return this.registries.values().stream();
    }

    @Override
    public <T> Optional<Registry<T>> getOptional(ResourceKey<? extends Registry<? extends T>> registryRef) {
        return Optional.ofNullable(this.registries.get(registryRef));
    }

    @Override
    public <T> void registerEntryAdded(ResourceKey<? extends Registry<? extends T>> registryRef, RegistryEntryAddedCallback<T> callback) {
        Registry<?> registry = this.registries.get(registryRef);
        if (registry != null) {
            RegistryEntryAddedCallback.event(registry).register(callback);
        }
    }
}


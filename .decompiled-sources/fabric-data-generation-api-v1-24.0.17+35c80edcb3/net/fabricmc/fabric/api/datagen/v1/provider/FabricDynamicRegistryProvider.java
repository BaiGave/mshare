/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FabricDynamicRegistryProvider
implements DataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricDynamicRegistryProvider.class);
    private final FabricPackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    public FabricDynamicRegistryProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        this.output = output;
        this.registriesFuture = registriesFuture;
    }

    protected abstract void configure(HolderLookup.Provider var1, Entries var2);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return this.registriesFuture.thenCompose(registries -> CompletableFuture.supplyAsync(() -> {
            Entries entries = new Entries((HolderLookup.Provider)registries, this.output.getModId());
            this.configure((HolderLookup.Provider)registries, entries);
            return entries;
        }).thenCompose(entries -> {
            RegistryOps<JsonElement> dynamicOps = registries.createSerializationContext(JsonOps.INSTANCE);
            ArrayList futures = new ArrayList();
            for (RegistryEntries<?> registryEntries : entries.queuedEntries.values()) {
                futures.add(this.writeHolders(cache, dynamicOps, registryEntries));
            }
            return CompletableFuture.allOf((CompletableFuture[])futures.toArray(CompletableFuture[]::new));
        }));
    }

    private <T> CompletableFuture<?> writeHolders(CachedOutput cache, RegistryOps<JsonElement> ops, RegistryEntries<T> registryEntries) {
        ResourceKey registry = registryEntries.registry;
        boolean shouldOmitNamespace = registry.identifier().getNamespace().equals("minecraft") || !DynamicRegistriesImpl.FABRIC_DYNAMIC_REGISTRY_KEYS.contains(registry);
        String directoryName = shouldOmitNamespace ? registry.identifier().getPath() : registry.identifier().getNamespace() + "/" + registry.identifier().getPath();
        PackOutput.PathProvider pathResolver = this.output.createPathProvider(PackOutput.Target.DATA_PACK, directoryName);
        ArrayList futures = new ArrayList();
        for (Map.Entry entry : registryEntries.resources.entrySet()) {
            Path path = pathResolver.json(entry.getKey().identifier());
            futures.add(FabricDynamicRegistryProvider.writeToPath(path, cache, ops, registryEntries.elementCodec, entry.getValue().value(), entry.getValue().conditions()));
        }
        return CompletableFuture.allOf((CompletableFuture[])futures.toArray(CompletableFuture[]::new));
    }

    private static <E> CompletableFuture<?> writeToPath(Path path, CachedOutput cache, DynamicOps<JsonElement> json, Encoder<E> encoder, E value, @Nullable ResourceCondition[] conditions) {
        Optional<JsonElement> optional = encoder.encodeStart(json, value).resultOrPartial(error -> LOGGER.error("Couldn't serialize element {}: {}", (Object)path, error));
        if (optional.isPresent()) {
            JsonElement jsonElement = optional.get();
            if (conditions != null && conditions.length > 0) {
                if (!jsonElement.isJsonObject()) {
                    throw new IllegalStateException("Cannot add conditions to " + String.valueOf(path) + ": JSON is a non-object value");
                }
                FabricDataGenHelper.addConditions(jsonElement.getAsJsonObject(), conditions);
            }
            return DataProvider.saveStable(cache, jsonElement, path);
        }
        return CompletableFuture.completedFuture(null);
    }

    private static class RegistryEntries<T> {
        final HolderOwner<T> lookup;
        final ResourceKey<? extends Registry<T>> registry;
        final Codec<T> elementCodec;
        Map<ResourceKey<T>, ConditionalEntry<T>> resources = new IdentityHashMap<ResourceKey<T>, ConditionalEntry<T>>();

        RegistryEntries(HolderOwner<T> lookup, ResourceKey<? extends Registry<T>> registry, Codec<T> elementCodec) {
            this.lookup = lookup;
            this.registry = registry;
            this.elementCodec = elementCodec;
        }

        static <T> RegistryEntries<T> create(HolderLookup.Provider registryLookups, RegistryDataLoader.RegistryData<T> loaderEntry) {
            HolderGetter lookup = registryLookups.lookupOrThrow(loaderEntry.key());
            return new RegistryEntries<T>(lookup, loaderEntry.key(), loaderEntry.elementCodec());
        }

        Holder<T> add(ResourceKey<T> key, T value, @Nullable ResourceCondition[] conditions) {
            if (this.resources.put(key, new ConditionalEntry<T>(value, conditions)) != null) {
                throw new IllegalArgumentException("Trying to add resource key " + String.valueOf(key) + " more than once.");
            }
            return Holder.Reference.createStandAlone(this.lookup, key);
        }
    }

    private record ConditionalEntry<T>(T value, @Nullable ResourceCondition[] conditions) {
    }

    public static final class Entries {
        private final HolderLookup.Provider registries;
        private final Map<Identifier, RegistryEntries<?>> queuedEntries;
        private final String modId;

        @ApiStatus.Internal
        Entries(HolderLookup.Provider registries, String modId) {
            this.registries = registries;
            this.queuedEntries = DynamicRegistries.getDynamicRegistries().stream().filter(e -> registries.lookup(e.key()).isPresent()).collect(Collectors.toMap(e -> e.key().identifier(), e -> RegistryEntries.create(registries, e)));
            this.modId = modId;
        }

        public HolderLookup.Provider getLookups() {
            return this.registries;
        }

        public <T> HolderGetter<T> getLookup(ResourceKey<? extends Registry<T>> registryKey) {
            return this.registries.lookupOrThrow(registryKey);
        }

        public HolderGetter<PlacedFeature> placedFeatures() {
            return this.getLookup(Registries.PLACED_FEATURE);
        }

        public HolderGetter<ConfiguredWorldCarver<?>> configuredCarvers() {
            return this.getLookup(Registries.CONFIGURED_CARVER);
        }

        public <T> Holder<T> ref(ResourceKey<T> key) {
            RegistryEntries<T> entries = this.getQueuedEntries(key);
            return Holder.Reference.createStandAlone(entries.lookup, key);
        }

        public <T> Holder<T> add(ResourceKey<T> key, T object) {
            return this.getQueuedEntries(key).add(key, object, null);
        }

        public <T> Holder<T> add(ResourceKey<T> key, T object, ResourceCondition ... conditions) {
            return this.getQueuedEntries(key).add(key, object, conditions);
        }

        public <T> void add(Holder.Reference<T> object) {
            this.add(object.key(), object.value());
        }

        public <T> void add(Holder.Reference<T> object, ResourceCondition ... conditions) {
            this.add(object.key(), object.value(), conditions);
        }

        public <T> Holder<T> add(HolderLookup.RegistryLookup<T> registry, ResourceKey<T> valueKey) {
            return this.add(valueKey, registry.getOrThrow(valueKey).value());
        }

        public <T> Holder<T> add(HolderLookup.RegistryLookup<T> registry, ResourceKey<T> valueKey, ResourceCondition ... conditions) {
            return this.add(valueKey, registry.getOrThrow(valueKey).value(), conditions);
        }

        public <T> List<Holder<T>> addAll(HolderLookup.RegistryLookup<T> registry) {
            return registry.listElementIds().filter(resourceKey -> resourceKey.identifier().getNamespace().equals(this.modId)).map(key -> this.add(registry, (ResourceKey)key)).toList();
        }

        <T> RegistryEntries<T> getQueuedEntries(ResourceKey<T> key) {
            RegistryEntries<?> regEntries = this.queuedEntries.get(key.registry());
            if (regEntries == null) {
                throw new IllegalArgumentException("Registry " + String.valueOf(key.registry()) + " is not loaded from datapacks");
            }
            return regEntries;
        }
    }
}


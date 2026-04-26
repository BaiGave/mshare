/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1.provider;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

public abstract class FabricCodecDataProvider<T>
implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;
    private final Codec<T> codec;

    private FabricCodecDataProvider(PackOutput.PathProvider pathProvider, CompletableFuture<HolderLookup.Provider> registriesFuture, Codec<T> codec) {
        this.pathProvider = pathProvider;
        this.registriesFuture = Objects.requireNonNull(registriesFuture);
        this.codec = codec;
    }

    protected FabricCodecDataProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture, PackOutput.Target target, String directoryName, Codec<T> codec) {
        this(packOutput.createPathProvider(target, directoryName), registriesFuture, codec);
    }

    protected FabricCodecDataProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture, ResourceKey<? extends Registry<?>> key, Codec<T> codec) {
        this(packOutput.createRegistryElementsPathProvider(key), registriesFuture, codec);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registriesFuture.thenCompose(lookup -> {
            HashMap<Identifier, JsonElement> entries = new HashMap<Identifier, JsonElement>();
            RegistryOps<JsonElement> ops = lookup.createSerializationContext(JsonOps.INSTANCE);
            BiConsumer<Identifier, Object> provider = (id, value) -> {
                JsonElement json = this.convert((Identifier)id, (T)value, (DynamicOps<JsonElement>)ops);
                JsonElement existingJson = entries.put((Identifier)id, json);
                if (existingJson != null) {
                    throw new IllegalArgumentException("Duplicate entry " + String.valueOf(id));
                }
            };
            this.configure((BiConsumer<Identifier, T>)provider, (HolderLookup.Provider)lookup);
            return this.write(output, entries);
        });
    }

    protected abstract void configure(BiConsumer<Identifier, T> var1, HolderLookup.Provider var2);

    private JsonElement convert(Identifier id, T value, DynamicOps<JsonElement> ops) {
        DataResult<JsonElement> dataResult = this.codec.encodeStart(ops, (JsonElement)value);
        return dataResult.mapError(message -> "Invalid entry %s: %s".formatted(id, message)).getOrThrow();
    }

    private CompletableFuture<?> write(CachedOutput output, Map<Identifier, JsonElement> entries) {
        return CompletableFuture.allOf((CompletableFuture[])entries.entrySet().stream().map(entry -> {
            Path path = this.pathProvider.json((Identifier)entry.getKey());
            return DataProvider.saveStable(output, (JsonElement)entry.getValue(), path);
        }).toArray(CompletableFuture[]::new));
    }
}


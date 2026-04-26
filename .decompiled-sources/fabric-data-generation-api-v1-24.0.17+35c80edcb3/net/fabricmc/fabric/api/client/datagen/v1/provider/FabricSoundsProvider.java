/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.datagen.v1.provider;

import com.mojang.serialization.Codec;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.fabricmc.fabric.impl.datagen.client.SoundTypeBuilderImpl;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.ApiStatus;

public abstract class FabricSoundsProvider
implements DataProvider {
    private static final Codec<Map<String, SoundTypeBuilderImpl.SoundType>> CODEC = Codec.unboundedMap(Codec.STRING, SoundTypeBuilderImpl.SoundType.CODEC);
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;
    private final PackOutput output;

    public FabricSoundsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        this.registriesFuture = registriesFuture;
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registriesFuture.thenCompose(lookup -> {
            LinkedHashMap data = new LinkedHashMap();
            this.configure((HolderLookup.Provider)lookup, (id, builder) -> {
                if (data.computeIfAbsent(id.getNamespace(), n -> new LinkedHashMap()).put(id.getPath(), ((SoundTypeBuilderImpl)builder).build()) != null) {
                    throw new IllegalStateException("Duplicate sound for event " + String.valueOf(id));
                }
            });
            return CompletableFuture.allOf((CompletableFuture[])data.entrySet().stream().map(file -> {
                Path outputPath = this.output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve((String)file.getKey() + "/sounds.json");
                return DataProvider.saveStable(output, lookup, CODEC, (Map)file.getValue(), outputPath);
            }).toArray(CompletableFuture[]::new));
        });
    }

    protected abstract void configure(HolderLookup.Provider var1, SoundExporter var2);

    @FunctionalInterface
    @ApiStatus.NonExtendable
    public static interface SoundExporter {
        default public void add(SoundEvent event, SoundTypeBuilder builder) {
            this.add(event.location(), builder);
        }

        default public void add(Holder<SoundEvent> event, SoundTypeBuilder builder) {
            this.add(event.unwrapKey().orElseThrow(() -> new IllegalArgumentException("Direct (non-registered) sound event cannot be added")).identifier(), builder);
        }

        public void add(Identifier var1, SoundTypeBuilder var2);
    }
}


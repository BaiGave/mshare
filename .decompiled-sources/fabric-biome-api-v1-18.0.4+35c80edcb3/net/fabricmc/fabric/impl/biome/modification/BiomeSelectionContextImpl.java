/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.biome.modification;

import java.util.Optional;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public class BiomeSelectionContextImpl
implements BiomeSelectionContext {
    private final RegistryAccess dynamicRegistries;
    private final ResourceKey<Biome> key;
    private final Biome biome;
    private final Holder<Biome> entry;

    public BiomeSelectionContextImpl(RegistryAccess dynamicRegistries, ResourceKey<Biome> key, Biome biome) {
        this.dynamicRegistries = dynamicRegistries;
        this.key = key;
        this.biome = biome;
        this.entry = dynamicRegistries.lookupOrThrow(Registries.BIOME).getOrThrow(this.key);
    }

    @Override
    public ResourceKey<Biome> getBiomeKey() {
        return this.key;
    }

    @Override
    public Biome getBiome() {
        return this.biome;
    }

    @Override
    public Holder<Biome> getBiomeHolder() {
        return this.entry;
    }

    @Override
    public Optional<ResourceKey<ConfiguredFeature<?, ?>>> getFeatureKey(ConfiguredFeature<?, ?> configuredFeature) {
        HolderLookup.RegistryLookup registry = this.dynamicRegistries.lookupOrThrow(Registries.CONFIGURED_FEATURE);
        return registry.getResourceKey(configuredFeature);
    }

    @Override
    public Optional<ResourceKey<PlacedFeature>> getPlacedFeatureKey(PlacedFeature placedFeature) {
        HolderLookup.RegistryLookup registry = this.dynamicRegistries.lookupOrThrow(Registries.PLACED_FEATURE);
        return registry.getResourceKey(placedFeature);
    }

    @Override
    public boolean validForStructure(ResourceKey<Structure> key) {
        Structure instance = this.dynamicRegistries.lookupOrThrow(Registries.STRUCTURE).getValue(key);
        if (instance == null) {
            return false;
        }
        return instance.biomes().contains(this.getBiomeHolder());
    }

    @Override
    public Optional<ResourceKey<Structure>> getStructureKey(Structure structure) {
        HolderLookup.RegistryLookup registry = this.dynamicRegistries.lookupOrThrow(Registries.STRUCTURE);
        return registry.getResourceKey(structure);
    }

    @Override
    public boolean canGenerateIn(ResourceKey<LevelStem> levelStemKey) {
        LevelStem dimension = this.dynamicRegistries.lookupOrThrow(Registries.LEVEL_STEM).getValue(levelStemKey);
        if (dimension == null) {
            return false;
        }
        return dimension.generator().getBiomeSource().possibleBiomes().stream().anyMatch(entry -> entry.value() == this.biome);
    }

    @Override
    public boolean hasTag(TagKey<Biome> tag) {
        HolderLookup.RegistryLookup biomeRegistry = this.dynamicRegistries.lookupOrThrow(Registries.BIOME);
        return biomeRegistry.getOrThrow(this.getBiomeKey()).is(tag);
    }
}


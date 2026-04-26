/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.biome.v1;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface BiomeSelectionContext {
    public ResourceKey<Biome> getBiomeKey();

    public Biome getBiome();

    public Holder<Biome> getBiomeHolder();

    default public boolean hasFeature(ResourceKey<ConfiguredFeature<?, ?>> key) {
        List<HolderSet<PlacedFeature>> featureSteps = this.getBiome().getGenerationSettings().features();
        for (HolderSet<PlacedFeature> featureSuppliers : featureSteps) {
            for (Holder holder : featureSuppliers) {
                if (!((PlacedFeature)holder.value()).getFeatures().anyMatch(cf -> this.getFeatureKey((ConfiguredFeature)cf.value()).orElse(null) == key)) continue;
                return true;
            }
        }
        return false;
    }

    default public boolean hasPlacedFeature(ResourceKey<PlacedFeature> key) {
        List<HolderSet<PlacedFeature>> featureSteps = this.getBiome().getGenerationSettings().features();
        for (HolderSet<PlacedFeature> featureSuppliers : featureSteps) {
            for (Holder holder : featureSuppliers) {
                if (this.getPlacedFeatureKey((PlacedFeature)holder.value()).orElse(null) != key) continue;
                return true;
            }
        }
        return false;
    }

    public Optional<ResourceKey<ConfiguredFeature<?, ?>>> getFeatureKey(ConfiguredFeature<?, ?> var1);

    public Optional<ResourceKey<PlacedFeature>> getPlacedFeatureKey(PlacedFeature var1);

    public boolean validForStructure(ResourceKey<Structure> var1);

    public Optional<ResourceKey<Structure>> getStructureKey(Structure var1);

    public boolean canGenerateIn(ResourceKey<LevelStem> var1);

    public boolean hasTag(TagKey<Biome> var1);
}


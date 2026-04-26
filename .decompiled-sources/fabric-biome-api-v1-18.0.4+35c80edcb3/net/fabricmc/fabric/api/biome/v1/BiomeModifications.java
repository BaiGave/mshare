/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.biome.v1;

import com.google.common.base.Preconditions;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class BiomeModifications {
    private BiomeModifications() {
    }

    public static void addFeature(Predicate<BiomeSelectionContext> biomeSelector, GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey) {
        BiomeModifications.create(placedFeatureKey.identifier()).add(ModificationPhase.ADDITIONS, biomeSelector, context -> context.getGenerationSettings().addFeature(step, placedFeatureKey));
    }

    public static void addCarver(Predicate<BiomeSelectionContext> biomeSelector, ResourceKey<ConfiguredWorldCarver<?>> configuredCarverKey) {
        BiomeModifications.create(configuredCarverKey.identifier()).add(ModificationPhase.ADDITIONS, biomeSelector, context -> context.getGenerationSettings().addCarver(configuredCarverKey));
    }

    public static void addSpawn(Predicate<BiomeSelectionContext> biomeSelector, MobCategory category, EntityType<?> entityType, int weight, int minGroupSize, int maxGroupSize) {
        Preconditions.checkArgument(entityType.getCategory() != MobCategory.MISC, "Cannot add spawns for entities with category=MISC since they'd be replaced by pigs.");
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        Preconditions.checkState(BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityType).isPresent(), "Unregistered entity type: %s", entityType);
        BiomeModifications.create(id).add(ModificationPhase.ADDITIONS, biomeSelector, context -> context.getMobSpawnSettings().addSpawn(category, new MobSpawnSettings.SpawnerData(entityType, minGroupSize, maxGroupSize), weight));
    }

    public static BiomeModification create(Identifier id) {
        return new BiomeModification(id);
    }
}


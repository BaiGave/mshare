/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.biome.v1;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.impl.biome.modification.BuiltInResourceKeys;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.LevelStem;

public final class BiomeSelectors {
    private BiomeSelectors() {
    }

    public static Predicate<BiomeSelectionContext> all() {
        return context -> true;
    }

    public static Predicate<BiomeSelectionContext> vanilla() {
        return context -> context.getBiomeKey().identifier().getNamespace().equals("minecraft") && BuiltInResourceKeys.isBuiltinBiome(context.getBiomeKey());
    }

    public static Predicate<BiomeSelectionContext> foundInOverworld() {
        return context -> context.canGenerateIn(LevelStem.OVERWORLD);
    }

    public static Predicate<BiomeSelectionContext> foundInTheNether() {
        return context -> context.canGenerateIn(LevelStem.NETHER);
    }

    public static Predicate<BiomeSelectionContext> foundInTheEnd() {
        return context -> context.canGenerateIn(LevelStem.END);
    }

    public static Predicate<BiomeSelectionContext> tag(TagKey<Biome> tag) {
        return context -> context.hasTag(tag);
    }

    @SafeVarargs
    public static Predicate<BiomeSelectionContext> excludeByKey(ResourceKey<Biome> ... keys) {
        return BiomeSelectors.excludeByKey(ImmutableSet.copyOf(keys));
    }

    public static Predicate<BiomeSelectionContext> excludeByKey(Collection<ResourceKey<Biome>> keys) {
        return context -> !keys.contains(context.getBiomeKey());
    }

    @SafeVarargs
    public static Predicate<BiomeSelectionContext> includeByKey(ResourceKey<Biome> ... keys) {
        return BiomeSelectors.includeByKey(ImmutableSet.copyOf(keys));
    }

    public static Predicate<BiomeSelectionContext> includeByKey(Collection<ResourceKey<Biome>> keys) {
        return context -> keys.contains(context.getBiomeKey());
    }

    public static Predicate<BiomeSelectionContext> spawnsOneOf(EntityType<?> ... entityTypes) {
        return BiomeSelectors.spawnsOneOf(ImmutableSet.copyOf(entityTypes));
    }

    public static Predicate<BiomeSelectionContext> spawnsOneOf(Set<EntityType<?>> entityTypes) {
        return context -> {
            MobSpawnSettings spawnSettings = context.getBiome().getMobSettings();
            for (MobCategory mobCategory : MobCategory.values()) {
                for (Weighted<MobSpawnSettings.SpawnerData> spawnEntry : spawnSettings.getMobs(mobCategory).unwrap()) {
                    if (!entityTypes.contains(spawnEntry.value().type())) continue;
                    return true;
                }
            }
            return false;
        };
    }
}


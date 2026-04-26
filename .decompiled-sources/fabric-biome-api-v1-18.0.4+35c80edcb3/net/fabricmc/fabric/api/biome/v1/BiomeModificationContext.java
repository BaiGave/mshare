/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.biome.v1;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.UnmodifiableView;

public interface BiomeModificationContext {
    public WeatherContext getWeather();

    public AttributesContext getAttributes();

    public EffectsContext getEffects();

    public GenerationSettingsContext getGenerationSettings();

    public MobSpawnSettingsContext getMobSpawnSettings();

    public static interface MobSpawnSettingsContext {
        public void setCreatureGenerationProbability(float var1);

        public @UnmodifiableView List<Weighted<MobSpawnSettings.SpawnerData>> getMobs(MobCategory var1);

        public void addSpawn(MobCategory var1, MobSpawnSettings.SpawnerData var2, int var3);

        public boolean removeSpawns(BiPredicate<MobCategory, MobSpawnSettings.SpawnerData> var1);

        default public boolean removeSpawnsOfEntityType(EntityType<?> entityType) {
            return this.removeSpawns((category, spawnEntry) -> spawnEntry.type() == entityType);
        }

        default public void clearSpawns(MobCategory category) {
            this.removeSpawns((mobCategory, spawnEntry) -> mobCategory == category);
        }

        default public void clearSpawns() {
            this.removeSpawns((mobCategory, spawnEntry) -> true);
        }

        public void addMobCharge(EntityType<?> var1, double var2, double var4);

        public void clearMobCharge(EntityType<?> var1);
    }

    public static interface GenerationSettingsContext {
        public boolean removeFeature(GenerationStep.Decoration var1, ResourceKey<PlacedFeature> var2);

        default public boolean removeFeature(ResourceKey<PlacedFeature> placedFeatureKey) {
            boolean anyFound = false;
            for (GenerationStep.Decoration step : GenerationStep.Decoration.values()) {
                if (!this.removeFeature(step, placedFeatureKey)) continue;
                anyFound = true;
            }
            return anyFound;
        }

        public void addFeature(GenerationStep.Decoration var1, ResourceKey<PlacedFeature> var2);

        public void addCarver(ResourceKey<ConfiguredWorldCarver<?>> var1);

        public boolean removeCarver(ResourceKey<ConfiguredWorldCarver<?>> var1);
    }

    public static interface EffectsContext {
        @Deprecated
        public void setFogColor(int var1);

        public void setWaterColor(int var1);

        @Deprecated
        public void setWaterFogColor(int var1);

        @Deprecated
        public void setSkyColor(int var1);

        public void setFoliageColorOverride(Optional<Integer> var1);

        default public void setFoliageColorOverride(int color) {
            this.setFoliageColorOverride(Optional.of(color));
        }

        default public void setFoliageColorOverride(OptionalInt color) {
            color.ifPresentOrElse(this::setFoliageColorOverride, this::clearFoliageColorOverride);
        }

        default public void clearFoliageColorOverride() {
            this.setFoliageColorOverride(Optional.empty());
        }

        public void setDryFoliageColorOverride(Optional<Integer> var1);

        default public void setDryFoliageColorOverride(int color) {
            this.setDryFoliageColorOverride(Optional.of(color));
        }

        default public void setDryFoliageColorOverride(OptionalInt color) {
            color.ifPresentOrElse(this::setDryFoliageColorOverride, this::clearDryFoliageColorOverride);
        }

        default public void clearDryFoliageColorOverride() {
            this.setDryFoliageColorOverride(Optional.empty());
        }

        public void setGrassColorOverride(Optional<Integer> var1);

        default public void setGrassColorOverride(int color) {
            this.setGrassColorOverride(Optional.of(color));
        }

        default public void setGrassColorOverride(OptionalInt color) {
            color.ifPresentOrElse(this::setGrassColorOverride, this::clearGrassColorOverride);
        }

        default public void clearGrassColorOverride() {
            this.setGrassColorOverride(Optional.empty());
        }

        public void setGrassColorModifier(BiomeSpecialEffects.GrassColorModifier var1);

        @Deprecated
        public void setMusicVolume(float var1);
    }

    public static interface AttributesContext {
        public void addAll(EnvironmentAttributeMap var1);

        default public void addAll(EnvironmentAttributeMap.Builder map) {
            this.addAll(map.build());
        }

        public <T> void set(EnvironmentAttribute<T> var1, T var2);

        public <T, M> void setModifier(EnvironmentAttribute<T> var1, AttributeModifier<T, M> var2, M var3);
    }

    public static interface WeatherContext {
        public void setPrecipitation(boolean var1);

        public void setTemperature(float var1);

        public void setTemperatureModifier(Biome.TemperatureModifier var1);

        public void setDownfall(float var1);
    }
}


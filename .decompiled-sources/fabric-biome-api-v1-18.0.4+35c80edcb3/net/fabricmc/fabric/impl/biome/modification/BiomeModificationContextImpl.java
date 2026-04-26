/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.biome.modification;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FeatureTags;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

public class BiomeModificationContextImpl
implements BiomeModificationContext {
    private final RegistryAccess registries;
    private final Biome biome;
    private final BiomeModificationContext.WeatherContext weather;
    private final BiomeModificationContext.AttributesContext attributes;
    private final BiomeModificationContext.EffectsContext effects;
    private final GenerationSettingsContextImpl generationSettings;
    private final SpawnSettingsContextImpl spawnSettings;

    public BiomeModificationContextImpl(RegistryAccess registries, Biome biome) {
        this.registries = registries;
        this.biome = biome;
        this.weather = new WeatherContextImpl(this);
        this.attributes = new AttributesContextImpl(this);
        this.effects = new EffectsContextImpl(this);
        this.generationSettings = new GenerationSettingsContextImpl(this);
        this.spawnSettings = new SpawnSettingsContextImpl(this);
    }

    @Override
    public BiomeModificationContext.WeatherContext getWeather() {
        return this.weather;
    }

    @Override
    public BiomeModificationContext.AttributesContext getAttributes() {
        return this.attributes;
    }

    @Override
    public BiomeModificationContext.EffectsContext getEffects() {
        return this.effects;
    }

    @Override
    public BiomeModificationContext.GenerationSettingsContext getGenerationSettings() {
        return this.generationSettings;
    }

    @Override
    public BiomeModificationContext.MobSpawnSettingsContext getMobSpawnSettings() {
        return this.spawnSettings;
    }

    void freeze() {
        this.generationSettings.freeze();
        this.spawnSettings.freeze();
    }

    boolean shouldRebuildFeatures() {
        return this.generationSettings.rebuildFeatures;
    }

    private static <T> Holder.Reference<T> getHolder(Registry<T> registry, ResourceKey<T> key) {
        Holder.Reference holder = registry.get(key).orElse(null);
        if (holder == null) {
            throw new IllegalArgumentException("Couldn't find holder for " + String.valueOf(key));
        }
        return holder;
    }

    private class WeatherContextImpl
    implements BiomeModificationContext.WeatherContext {
        final /* synthetic */ BiomeModificationContextImpl this$0;

        private WeatherContextImpl(BiomeModificationContextImpl biomeModificationContextImpl) {
            BiomeModificationContextImpl biomeModificationContextImpl2 = biomeModificationContextImpl;
            Objects.requireNonNull(biomeModificationContextImpl2);
            this.this$0 = biomeModificationContextImpl2;
        }

        @Override
        public void setPrecipitation(boolean hasPrecipitation) {
            this.this$0.biome.climateSettings = new Biome.ClimateSettings(hasPrecipitation, this.this$0.biome.climateSettings.temperature(), this.this$0.biome.climateSettings.temperatureModifier(), this.this$0.biome.climateSettings.downfall());
        }

        @Override
        public void setTemperature(float temperature) {
            this.this$0.biome.climateSettings = new Biome.ClimateSettings(this.this$0.biome.climateSettings.hasPrecipitation(), temperature, this.this$0.biome.climateSettings.temperatureModifier(), this.this$0.biome.climateSettings.downfall());
        }

        @Override
        public void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier) {
            this.this$0.biome.climateSettings = new Biome.ClimateSettings(this.this$0.biome.climateSettings.hasPrecipitation(), this.this$0.biome.climateSettings.temperature(), Objects.requireNonNull(temperatureModifier), this.this$0.biome.climateSettings.downfall());
        }

        @Override
        public void setDownfall(float downfall) {
            this.this$0.biome.climateSettings = new Biome.ClimateSettings(this.this$0.biome.climateSettings.hasPrecipitation(), this.this$0.biome.climateSettings.temperature(), this.this$0.biome.climateSettings.temperatureModifier(), downfall);
        }
    }

    private class AttributesContextImpl
    implements BiomeModificationContext.AttributesContext {
        final /* synthetic */ BiomeModificationContextImpl this$0;

        private AttributesContextImpl(BiomeModificationContextImpl biomeModificationContextImpl) {
            BiomeModificationContextImpl biomeModificationContextImpl2 = biomeModificationContextImpl;
            Objects.requireNonNull(biomeModificationContextImpl2);
            this.this$0 = biomeModificationContextImpl2;
        }

        @Override
        public void addAll(EnvironmentAttributeMap map) {
            EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(this.this$0.biome.getAttributes());
            attributes.putAll(map);
            this.this$0.biome.attributes = attributes.build();
        }

        @Override
        public <T> void set(EnvironmentAttribute<T> key, T value) {
            EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(this.this$0.biome.getAttributes());
            attributes.set(key, value);
            this.this$0.biome.attributes = attributes.build();
        }

        @Override
        public <T, M> void setModifier(EnvironmentAttribute<T> key, AttributeModifier<T, M> modifier, M value) {
            EnvironmentAttributeMap.Builder attributes = EnvironmentAttributeMap.builder().putAll(this.this$0.biome.getAttributes());
            attributes.modify(key, modifier, value);
            this.this$0.biome.attributes = attributes.build();
        }
    }

    private class EffectsContextImpl
    implements BiomeModificationContext.EffectsContext {
        private final BiomeSpecialEffects effects;
        final /* synthetic */ BiomeModificationContextImpl this$0;

        private EffectsContextImpl(BiomeModificationContextImpl biomeModificationContextImpl) {
            BiomeModificationContextImpl biomeModificationContextImpl2 = biomeModificationContextImpl;
            Objects.requireNonNull(biomeModificationContextImpl2);
            this.this$0 = biomeModificationContextImpl2;
            this.effects = this.this$0.biome.getSpecialEffects();
        }

        @Override
        public void setFogColor(int color) {
            this.this$0.attributes.set(EnvironmentAttributes.FOG_COLOR, color);
        }

        @Override
        public void setWaterColor(int color) {
            this.effects.waterColor = color;
        }

        @Override
        public void setWaterFogColor(int color) {
            this.this$0.attributes.set(EnvironmentAttributes.WATER_FOG_COLOR, color);
        }

        @Override
        public void setSkyColor(int color) {
            this.this$0.attributes.set(EnvironmentAttributes.SKY_COLOR, color);
        }

        @Override
        public void setFoliageColorOverride(Optional<Integer> color) {
            this.effects.foliageColorOverride = Objects.requireNonNull(color);
        }

        @Override
        public void setDryFoliageColorOverride(Optional<Integer> color) {
            this.effects.dryFoliageColorOverride = Objects.requireNonNull(color);
        }

        @Override
        public void setGrassColorOverride(Optional<Integer> color) {
            this.effects.grassColorOverride = Objects.requireNonNull(color);
        }

        @Override
        public void setGrassColorModifier(BiomeSpecialEffects.GrassColorModifier colorModifier) {
            this.effects.grassColorModifier = Objects.requireNonNull(colorModifier);
        }

        @Override
        public void setMusicVolume(float volume) {
            this.this$0.attributes.set(EnvironmentAttributes.MUSIC_VOLUME, Float.valueOf(volume));
        }
    }

    private class GenerationSettingsContextImpl
    implements BiomeModificationContext.GenerationSettingsContext {
        private final Registry<ConfiguredWorldCarver<?>> carvers;
        private final Registry<PlacedFeature> features;
        private final BiomeGenerationSettings generationSettings;
        boolean rebuildFeatures;
        final /* synthetic */ BiomeModificationContextImpl this$0;

        GenerationSettingsContextImpl(BiomeModificationContextImpl biomeModificationContextImpl) {
            BiomeModificationContextImpl biomeModificationContextImpl2 = biomeModificationContextImpl;
            Objects.requireNonNull(biomeModificationContextImpl2);
            this.this$0 = biomeModificationContextImpl2;
            this.carvers = this.this$0.registries.lookupOrThrow(Registries.CONFIGURED_CARVER);
            this.features = this.this$0.registries.lookupOrThrow(Registries.PLACED_FEATURE);
            this.generationSettings = this.this$0.biome.getGenerationSettings();
            this.unfreezeFeatures();
            this.rebuildFeatures = false;
        }

        private void unfreezeFeatures() {
            this.generationSettings.features = new ArrayList<HolderSet<PlacedFeature>>(this.generationSettings.features);
        }

        public void freeze() {
            this.freezeFeatures();
            if (this.rebuildFeatures) {
                this.rebuildFlowerFeatures();
            }
        }

        private void freezeFeatures() {
            this.generationSettings.features = ImmutableList.copyOf(this.generationSettings.features);
            this.generationSettings.featureSet = Suppliers.memoize(() -> this.generationSettings.features.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet()));
        }

        private void rebuildFlowerFeatures() {
            this.generationSettings.boneMealFeatures = Suppliers.memoize(() -> this.generationSettings.features.stream().flatMap(HolderSet::stream).flatMap(feature -> ((PlacedFeature)feature.value()).getFeatures()).filter(feature -> feature.is(FeatureTags.CAN_SPAWN_FROM_BONE_MEAL)).map(Holder::value).collect(ImmutableList.toImmutableList()));
        }

        @Override
        public boolean removeFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> placedFeatureKey) {
            List<HolderSet<PlacedFeature>> featureSteps;
            PlacedFeature placedFeature = BiomeModificationContextImpl.getHolder(this.features, placedFeatureKey).value();
            int stepIndex = step.ordinal();
            if (stepIndex >= (featureSteps = this.generationSettings.features).size()) {
                return false;
            }
            HolderSet<PlacedFeature> featuresInStep = featureSteps.get(stepIndex);
            ArrayList<Holder<PlacedFeature>> features = new ArrayList<Holder<PlacedFeature>>(featuresInStep.stream().toList());
            if (features.removeIf(feature -> feature.value() == placedFeature)) {
                featureSteps.set(stepIndex, HolderSet.direct(features));
                this.rebuildFeatures = true;
                return true;
            }
            return false;
        }

        @Override
        public void addFeature(GenerationStep.Decoration step, ResourceKey<PlacedFeature> entry) {
            List<HolderSet<PlacedFeature>> featureSteps = this.generationSettings.features;
            int index = step.ordinal();
            while (index >= featureSteps.size()) {
                featureSteps.add(HolderSet.direct(Collections.emptyList()));
            }
            Holder.Reference<PlacedFeature> feature = BiomeModificationContextImpl.getHolder(this.features, entry);
            if (featureSteps.get(index).contains(feature)) {
                return;
            }
            featureSteps.set(index, this.plus(featureSteps.get(index), feature));
            this.rebuildFeatures = true;
        }

        @Override
        public void addCarver(ResourceKey<ConfiguredWorldCarver<?>> entry) {
            this.generationSettings.carvers = this.plus(this.generationSettings.carvers, BiomeModificationContextImpl.getHolder(this.carvers, entry));
        }

        @Override
        public boolean removeCarver(ResourceKey<ConfiguredWorldCarver<?>> carverKey) {
            ConfiguredWorldCarver<?> carver = BiomeModificationContextImpl.getHolder(this.carvers, carverKey).value();
            ArrayList genCarvers = new ArrayList(this.generationSettings.carvers.stream().toList());
            if (genCarvers.removeIf(entry -> entry.value() == carver)) {
                this.generationSettings.carvers = HolderSet.direct(genCarvers);
                return true;
            }
            return false;
        }

        private <T> HolderSet<T> plus(@Nullable HolderSet<T> values, Holder<T> holder) {
            if (values == null) {
                return HolderSet.direct(holder);
            }
            ArrayList<Holder<T>> list = new ArrayList<Holder<T>>(values.stream().toList());
            list.add(holder);
            return HolderSet.direct(list);
        }
    }

    private class SpawnSettingsContextImpl
    implements BiomeModificationContext.MobSpawnSettingsContext {
        private final MobSpawnSettings spawnSettings;
        private final EnumMap<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> fabricSpawners;
        final /* synthetic */ BiomeModificationContextImpl this$0;

        SpawnSettingsContextImpl(BiomeModificationContextImpl biomeModificationContextImpl) {
            BiomeModificationContextImpl biomeModificationContextImpl2 = biomeModificationContextImpl;
            Objects.requireNonNull(biomeModificationContextImpl2);
            this.this$0 = biomeModificationContextImpl2;
            this.spawnSettings = this.this$0.biome.getMobSettings();
            this.fabricSpawners = new EnumMap(MobCategory.class);
            this.unfreezeSpawners();
            this.unfreezeSpawnCost();
        }

        private void unfreezeSpawners() {
            this.fabricSpawners.clear();
            for (MobCategory mobCategory : MobCategory.values()) {
                WeightedList<MobSpawnSettings.SpawnerData> entries = this.spawnSettings.spawners.get(mobCategory);
                if (entries != null) {
                    this.fabricSpawners.put(mobCategory, new ArrayList<Weighted<MobSpawnSettings.SpawnerData>>(entries.unwrap()));
                    continue;
                }
                this.fabricSpawners.put(mobCategory, new ArrayList());
            }
        }

        private void unfreezeSpawnCost() {
            this.spawnSettings.mobSpawnCosts = new HashMap(this.spawnSettings.mobSpawnCosts);
        }

        public void freeze() {
            this.freezeSpawners();
            this.freezeSpawnCosts();
        }

        private void freezeSpawners() {
            HashMap<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>> spawners = new HashMap<MobCategory, WeightedList<MobSpawnSettings.SpawnerData>>(this.spawnSettings.spawners);
            for (Map.Entry<MobCategory, List<Weighted<MobSpawnSettings.SpawnerData>>> entry : this.fabricSpawners.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    spawners.put(entry.getKey(), WeightedList.of());
                    continue;
                }
                spawners.put(entry.getKey(), WeightedList.of(entry.getValue()));
            }
            this.spawnSettings.spawners = ImmutableMap.copyOf(spawners);
        }

        private void freezeSpawnCosts() {
            this.spawnSettings.mobSpawnCosts = ImmutableMap.copyOf(this.spawnSettings.mobSpawnCosts);
        }

        @Override
        public void setCreatureGenerationProbability(float probability) {
            this.spawnSettings.creatureGenerationProbability = probability;
        }

        @Override
        public @UnmodifiableView List<Weighted<MobSpawnSettings.SpawnerData>> getMobs(MobCategory category) {
            Objects.requireNonNull(category);
            return Collections.unmodifiableList(this.fabricSpawners.get(category));
        }

        @Override
        public void addSpawn(MobCategory category, MobSpawnSettings.SpawnerData data, int weight) {
            Objects.requireNonNull(category);
            Objects.requireNonNull(data);
            this.fabricSpawners.get(category).add(new Weighted<MobSpawnSettings.SpawnerData>(data, weight));
        }

        @Override
        public boolean removeSpawns(BiPredicate<MobCategory, MobSpawnSettings.SpawnerData> predicate) {
            boolean anyRemoved = false;
            for (MobCategory group : MobCategory.values()) {
                if (!this.fabricSpawners.get(group).removeIf(entry -> predicate.test(group, (MobSpawnSettings.SpawnerData)entry.value()))) continue;
                anyRemoved = true;
            }
            return anyRemoved;
        }

        @Override
        public void addMobCharge(EntityType<?> entityType, double charge, double energyBudget) {
            Objects.requireNonNull(entityType);
            this.spawnSettings.mobSpawnCosts.put(entityType, new MobSpawnSettings.MobSpawnCost(energyBudget, charge));
        }

        @Override
        public void clearMobCharge(EntityType<?> entityType) {
            this.spawnSettings.mobSpawnCosts.remove(entityType);
        }
    }
}


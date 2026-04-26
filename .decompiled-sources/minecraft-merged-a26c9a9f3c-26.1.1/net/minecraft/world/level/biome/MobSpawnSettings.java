/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MobSpawnSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float DEFAULT_CREATURE_SPAWN_PROBABILITY = 0.1f;
    public static final WeightedList<SpawnerData> EMPTY_MOB_LIST = WeightedList.of();
    public static final MobSpawnSettings EMPTY = new Builder().build();
    public static final MapCodec<MobSpawnSettings> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.floatRange(0.0f, 0.9999999f).optionalFieldOf("creature_spawn_probability", Float.valueOf(0.1f)).forGetter(b -> Float.valueOf(b.creatureGenerationProbability)), Codec.simpleMap(MobCategory.CODEC, WeightedList.codec(SpawnerData.CODEC).promotePartial((Consumer)Util.prefix("Spawn data: ", LOGGER::error)), StringRepresentable.keys(MobCategory.values())).fieldOf("spawners").forGetter(b -> b.spawners), Codec.simpleMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), MobSpawnCost.CODEC, BuiltInRegistries.ENTITY_TYPE).fieldOf("spawn_costs").forGetter(b -> b.mobSpawnCosts)).apply((Applicative<MobSpawnSettings, ?>)i, MobSpawnSettings::new));
    private final float creatureGenerationProbability;
    private final Map<MobCategory, WeightedList<SpawnerData>> spawners;
    private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts;

    private MobSpawnSettings(float creatureGenerationProbability, Map<MobCategory, WeightedList<SpawnerData>> spawners, Map<EntityType<?>, MobSpawnCost> mobSpawnCosts) {
        this.creatureGenerationProbability = creatureGenerationProbability;
        this.spawners = ImmutableMap.copyOf(spawners);
        this.mobSpawnCosts = ImmutableMap.copyOf(mobSpawnCosts);
    }

    public WeightedList<SpawnerData> getMobs(MobCategory category) {
        return this.spawners.getOrDefault(category, EMPTY_MOB_LIST);
    }

    public @Nullable MobSpawnCost getMobSpawnCost(EntityType<?> type) {
        return this.mobSpawnCosts.get(type);
    }

    public float getCreatureProbability() {
        return this.creatureGenerationProbability;
    }

    public record MobSpawnCost(double energyBudget, double charge) {
        public static final Codec<MobSpawnCost> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.DOUBLE.fieldOf("energy_budget")).forGetter(e -> e.energyBudget), ((MapCodec)Codec.DOUBLE.fieldOf("charge")).forGetter(e -> e.charge)).apply((Applicative<MobSpawnCost, ?>)i, MobSpawnCost::new));
    }

    public record SpawnerData(EntityType<?> type, int minCount, int maxCount) {
        public static final MapCodec<SpawnerData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type")).forGetter(d -> d.type), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("minCount")).forGetter(e -> e.minCount), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("maxCount")).forGetter(e -> e.maxCount)).apply((Applicative<SpawnerData, ?>)i, SpawnerData::new)).validate(spawnerData -> {
            if (spawnerData.minCount > spawnerData.maxCount) {
                return DataResult.error(() -> "minCount needs to be smaller or equal to maxCount");
            }
            return DataResult.success(spawnerData);
        });

        public SpawnerData {
            type = type.getCategory() == MobCategory.MISC ? EntityType.PIG : type;
        }

        @Override
        public String toString() {
            return String.valueOf(EntityType.getKey(this.type)) + "*(" + this.minCount + "-" + this.maxCount + ")";
        }
    }

    public static class Builder {
        private final Map<MobCategory, WeightedList.Builder<SpawnerData>> spawners = Util.makeEnumMap(MobCategory.class, c -> WeightedList.builder());
        private final Map<EntityType<?>, MobSpawnCost> mobSpawnCosts = Maps.newLinkedHashMap();
        private float creatureGenerationProbability = 0.1f;

        public Builder addSpawn(MobCategory category, int weight, SpawnerData spawnerData) {
            this.spawners.get(category).add(spawnerData, weight);
            return this;
        }

        public Builder addMobCharge(EntityType<?> type, double charge, double energyBudget) {
            this.mobSpawnCosts.put(type, new MobSpawnCost(energyBudget, charge));
            return this;
        }

        public Builder creatureGenerationProbability(float creatureGenerationProbability) {
            this.creatureGenerationProbability = creatureGenerationProbability;
            return this;
        }

        public MobSpawnSettings build() {
            return new MobSpawnSettings(this.creatureGenerationProbability, (Map<MobCategory, WeightedList<SpawnerData>>)this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> ((WeightedList.Builder)e.getValue()).build())), ImmutableMap.copyOf(this.mobSpawnCosts));
        }
    }
}


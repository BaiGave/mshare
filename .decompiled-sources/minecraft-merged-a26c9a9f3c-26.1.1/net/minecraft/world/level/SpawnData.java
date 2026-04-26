/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.level.LightLayer;

public record SpawnData(CompoundTag entityToSpawn, Optional<CustomSpawnRules> customSpawnRules, Optional<EquipmentTable> equipment) {
    public static final String ENTITY_TAG = "entity";
    public static final Codec<SpawnData> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)CompoundTag.CODEC.fieldOf(ENTITY_TAG)).forGetter(s -> s.entityToSpawn), CustomSpawnRules.CODEC.optionalFieldOf("custom_spawn_rules").forGetter(o -> o.customSpawnRules), EquipmentTable.CODEC.optionalFieldOf("equipment").forGetter(o -> o.equipment)).apply((Applicative<SpawnData, ?>)i, SpawnData::new));
    public static final Codec<WeightedList<SpawnData>> LIST_CODEC = WeightedList.codec(CODEC);

    public SpawnData() {
        this(new CompoundTag(), Optional.empty(), Optional.empty());
    }

    public SpawnData {
        Optional<Identifier> id = entityToSpawn.read("id", Identifier.CODEC);
        if (id.isPresent()) {
            entityToSpawn.store("id", Identifier.CODEC, id.get());
        } else {
            entityToSpawn.remove("id");
        }
    }

    public CompoundTag getEntityToSpawn() {
        return this.entityToSpawn;
    }

    public Optional<CustomSpawnRules> getCustomSpawnRules() {
        return this.customSpawnRules;
    }

    public Optional<EquipmentTable> getEquipment() {
        return this.equipment;
    }

    public record CustomSpawnRules(InclusiveRange<Integer> blockLightLimit, InclusiveRange<Integer> skyLightLimit) {
        private static final InclusiveRange<Integer> LIGHT_RANGE = new InclusiveRange<Integer>(0, 15);
        public static final Codec<CustomSpawnRules> CODEC = RecordCodecBuilder.create(i -> i.group(CustomSpawnRules.lightLimit("block_light_limit").forGetter(o -> o.blockLightLimit), CustomSpawnRules.lightLimit("sky_light_limit").forGetter(o -> o.skyLightLimit)).apply((Applicative<CustomSpawnRules, ?>)i, CustomSpawnRules::new));

        private static DataResult<InclusiveRange<Integer>> checkLightBoundaries(InclusiveRange<Integer> range) {
            if (!LIGHT_RANGE.contains(range)) {
                return DataResult.error(() -> "Light values must be withing range " + String.valueOf(LIGHT_RANGE));
            }
            return DataResult.success(range);
        }

        private static MapCodec<InclusiveRange<Integer>> lightLimit(String name) {
            return InclusiveRange.INT.lenientOptionalFieldOf(name, LIGHT_RANGE).validate(CustomSpawnRules::checkLightBoundaries);
        }

        public boolean isValidPosition(BlockPos blockSpawnPos, ServerLevel level) {
            return this.blockLightLimit.isValueInRange(level.getBrightness(LightLayer.BLOCK, blockSpawnPos)) && this.skyLightLimit.isValueInRange(level.getEffectiveSkyBrightness(blockSpawnPos));
        }
    }
}


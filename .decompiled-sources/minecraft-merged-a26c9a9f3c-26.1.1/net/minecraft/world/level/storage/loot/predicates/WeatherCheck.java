/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public record WeatherCheck(Optional<Boolean> isRaining, Optional<Boolean> isThundering) implements LootItemCondition
{
    public static final MapCodec<WeatherCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.BOOL.optionalFieldOf("raining").forGetter(WeatherCheck::isRaining), Codec.BOOL.optionalFieldOf("thundering").forGetter(WeatherCheck::isThundering)).apply((Applicative<WeatherCheck, ?>)i, WeatherCheck::new));

    public MapCodec<WeatherCheck> codec() {
        return MAP_CODEC;
    }

    @Override
    public boolean test(LootContext context) {
        ServerLevel level = context.getLevel();
        if (this.isRaining.isPresent() && this.isRaining.get().booleanValue() != level.isRaining()) {
            return false;
        }
        return !this.isThundering.isPresent() || this.isThundering.get().booleanValue() == level.isThundering();
    }

    public static Builder weather() {
        return new Builder();
    }

    public static class Builder
    implements LootItemCondition.Builder {
        private Optional<Boolean> isRaining = Optional.empty();
        private Optional<Boolean> isThundering = Optional.empty();

        public Builder setRaining(boolean raining) {
            this.isRaining = Optional.of(raining);
            return this;
        }

        public Builder setThundering(boolean thundering) {
            this.isThundering = Optional.of(thundering);
            return this;
        }

        @Override
        public WeatherCheck build() {
            return new WeatherCheck(this.isRaining, this.isThundering);
        }
    }
}


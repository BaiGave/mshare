/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.world.food.FoodData;

public record FoodPredicate(MinMaxBounds.Ints level, MinMaxBounds.Doubles saturation) {
    public static final FoodPredicate ANY = new FoodPredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Doubles.ANY);
    public static final Codec<FoodPredicate> CODEC = RecordCodecBuilder.create(i -> i.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("level", MinMaxBounds.Ints.ANY).forGetter(FoodPredicate::level), MinMaxBounds.Doubles.CODEC.optionalFieldOf("saturation", MinMaxBounds.Doubles.ANY).forGetter(FoodPredicate::saturation)).apply((Applicative<FoodPredicate, ?>)i, FoodPredicate::new));

    public boolean matches(FoodData food) {
        if (!this.level.matches(food.getFoodLevel())) {
            return false;
        }
        return this.saturation.matches(food.getSaturationLevel());
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Doubles saturation = MinMaxBounds.Doubles.ANY;

        public Builder withLevel(MinMaxBounds.Ints level) {
            this.level = level;
            return this;
        }

        public Builder withSaturation(MinMaxBounds.Doubles saturation) {
            this.saturation = saturation;
            return this;
        }

        public static Builder food() {
            return new Builder();
        }

        public FoodPredicate build() {
            return new FoodPredicate(this.level, this.saturation);
        }
    }
}


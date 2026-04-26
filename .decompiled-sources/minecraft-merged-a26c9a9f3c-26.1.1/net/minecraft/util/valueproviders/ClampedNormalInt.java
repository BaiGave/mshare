/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;

public record ClampedNormalInt(float mean, float deviation, int minInclusive, int maxInclusive) implements IntProvider
{
    public static final MapCodec<ClampedNormalInt> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("mean")).forGetter(ClampedNormalInt::mean), ((MapCodec)Codec.FLOAT.fieldOf("deviation")).forGetter(ClampedNormalInt::deviation), ((MapCodec)Codec.INT.fieldOf("min_inclusive")).forGetter(ClampedNormalInt::minInclusive), ((MapCodec)Codec.INT.fieldOf("max_inclusive")).forGetter(ClampedNormalInt::maxInclusive)).apply((Applicative<ClampedNormalInt, ?>)i, ClampedNormalInt::new)).validate(c -> {
        if (c.maxInclusive < c.minInclusive) {
            return DataResult.error(() -> "Max must be larger than min: [" + c.minInclusive + ", " + c.maxInclusive + "]");
        }
        return DataResult.success(c);
    });

    public static ClampedNormalInt of(float mean, float deviation, int minInclusive, int maxInclusive) {
        return new ClampedNormalInt(mean, deviation, minInclusive, maxInclusive);
    }

    @Override
    public int sample(RandomSource random) {
        return ClampedNormalInt.sample(random, this.mean, this.deviation, this.minInclusive, this.maxInclusive);
    }

    public static int sample(RandomSource random, float mean, float deviation, float minInclusive, float maxInclusive) {
        return (int)Mth.clamp(Mth.normal(random, mean, deviation), minInclusive, maxInclusive);
    }

    public MapCodec<ClampedNormalInt> codec() {
        return MAP_CODEC;
    }

    @Override
    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}


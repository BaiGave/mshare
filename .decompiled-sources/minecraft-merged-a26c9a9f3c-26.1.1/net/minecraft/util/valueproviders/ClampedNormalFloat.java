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
import net.minecraft.util.valueproviders.FloatProvider;

public record ClampedNormalFloat(float mean, float deviation, float min, float max) implements FloatProvider
{
    public static final MapCodec<ClampedNormalFloat> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("mean")).forGetter(ClampedNormalFloat::mean), ((MapCodec)Codec.FLOAT.fieldOf("deviation")).forGetter(ClampedNormalFloat::deviation), ((MapCodec)Codec.FLOAT.fieldOf("min")).forGetter(ClampedNormalFloat::min), ((MapCodec)Codec.FLOAT.fieldOf("max")).forGetter(ClampedNormalFloat::max)).apply((Applicative<ClampedNormalFloat, ?>)i, ClampedNormalFloat::new)).validate(c -> {
        if (c.max < c.min) {
            return DataResult.error(() -> "Max must be larger than min: [" + c.min + ", " + c.max + "]");
        }
        return DataResult.success(c);
    });

    public static ClampedNormalFloat of(float mean, float deviation, float min, float max) {
        return new ClampedNormalFloat(mean, deviation, min, max);
    }

    @Override
    public float sample(RandomSource random) {
        return ClampedNormalFloat.sample(random, this.mean, this.deviation, this.min, this.max);
    }

    public static float sample(RandomSource random, float mean, float deviation, float min, float max) {
        return Mth.clamp(Mth.normal(random, mean, deviation), min, max);
    }

    public MapCodec<ClampedNormalFloat> codec() {
        return MAP_CODEC;
    }

    @Override
    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
    }
}


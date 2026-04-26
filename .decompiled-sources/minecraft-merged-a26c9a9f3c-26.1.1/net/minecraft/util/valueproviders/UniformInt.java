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

public record UniformInt(int minInclusive, int maxInclusive) implements IntProvider
{
    public static final MapCodec<UniformInt> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.INT.fieldOf("min_inclusive")).forGetter(UniformInt::minInclusive), ((MapCodec)Codec.INT.fieldOf("max_inclusive")).forGetter(UniformInt::maxInclusive)).apply((Applicative<UniformInt, ?>)i, UniformInt::new)).validate(u -> {
        if (u.maxInclusive < u.minInclusive) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + u.minInclusive + ", max_inclusive: " + u.maxInclusive);
        }
        return DataResult.success(u);
    });

    public static UniformInt of(int minInclusive, int maxInclusive) {
        return new UniformInt(minInclusive, maxInclusive);
    }

    @Override
    public int sample(RandomSource random) {
        return Mth.randomBetweenInclusive(random, this.minInclusive, this.maxInclusive);
    }

    public MapCodec<UniformInt> codec() {
        return MAP_CODEC;
    }

    @Override
    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}


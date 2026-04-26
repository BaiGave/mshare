/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;

public record BiasedToBottomInt(int minInclusive, int maxInclusive) implements IntProvider
{
    public static final MapCodec<BiasedToBottomInt> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.INT.fieldOf("min_inclusive")).forGetter(BiasedToBottomInt::minInclusive), ((MapCodec)Codec.INT.fieldOf("max_inclusive")).forGetter(BiasedToBottomInt::maxInclusive)).apply((Applicative<BiasedToBottomInt, ?>)i, BiasedToBottomInt::new)).validate(u -> {
        if (u.maxInclusive < u.minInclusive) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + u.minInclusive + ", max_inclusive: " + u.maxInclusive);
        }
        return DataResult.success(u);
    });

    public static BiasedToBottomInt of(int minInclusive, int maxInclusive) {
        return new BiasedToBottomInt(minInclusive, maxInclusive);
    }

    @Override
    public int sample(RandomSource random) {
        return this.minInclusive + random.nextInt(random.nextInt(this.maxInclusive - this.minInclusive + 1) + 1);
    }

    public MapCodec<BiasedToBottomInt> codec() {
        return MAP_CODEC;
    }

    @Override
    public String toString() {
        return "[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}


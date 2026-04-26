/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import org.slf4j.Logger;

public class BiasedToBottomHeight
extends HeightProvider {
    public static final MapCodec<BiasedToBottomHeight> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)VerticalAnchor.CODEC.fieldOf("min_inclusive")).forGetter(u -> u.minInclusive), ((MapCodec)VerticalAnchor.CODEC.fieldOf("max_inclusive")).forGetter(u -> u.maxInclusive), Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("inner", 1).forGetter(u -> u.inner)).apply((Applicative<BiasedToBottomHeight, ?>)i, BiasedToBottomHeight::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final int inner;

    private BiasedToBottomHeight(VerticalAnchor minInclusive, VerticalAnchor maxInclusive, int inner) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        this.inner = inner;
    }

    public static BiasedToBottomHeight of(VerticalAnchor minInclusive, VerticalAnchor maxInclusive, int offset) {
        return new BiasedToBottomHeight(minInclusive, maxInclusive, offset);
    }

    @Override
    public int sample(RandomSource random, WorldGenerationContext context) {
        int min = this.minInclusive.resolveY(context);
        int max = this.maxInclusive.resolveY(context);
        if (max - min - this.inner + 1 <= 0) {
            LOGGER.warn("Empty height range: {}", (Object)this);
            return min;
        }
        int limit = random.nextInt(max - min - this.inner + 1);
        return random.nextInt(limit + this.inner) + min;
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.BIASED_TO_BOTTOM;
    }

    public String toString() {
        return "biased[" + String.valueOf(this.minInclusive) + "-" + String.valueOf(this.maxInclusive) + " inner: " + this.inner + "]";
    }
}


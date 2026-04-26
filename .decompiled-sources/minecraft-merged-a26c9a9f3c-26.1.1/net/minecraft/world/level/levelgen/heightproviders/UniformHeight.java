/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import org.slf4j.Logger;

public class UniformHeight
extends HeightProvider {
    public static final MapCodec<UniformHeight> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)VerticalAnchor.CODEC.fieldOf("min_inclusive")).forGetter(u -> u.minInclusive), ((MapCodec)VerticalAnchor.CODEC.fieldOf("max_inclusive")).forGetter(u -> u.maxInclusive)).apply((Applicative<UniformHeight, ?>)i, UniformHeight::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    private final VerticalAnchor minInclusive;
    private final VerticalAnchor maxInclusive;
    private final LongSet warnedFor = new LongOpenHashSet();

    private UniformHeight(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    public static UniformHeight of(VerticalAnchor minInclusive, VerticalAnchor maxInclusive) {
        return new UniformHeight(minInclusive, maxInclusive);
    }

    @Override
    public int sample(RandomSource random, WorldGenerationContext context) {
        int max;
        int min = this.minInclusive.resolveY(context);
        if (min > (max = this.maxInclusive.resolveY(context))) {
            if (this.warnedFor.add((long)min << 32 | (long)max)) {
                LOGGER.warn("Empty height range: {}", (Object)this);
            }
            return min;
        }
        return Mth.randomBetweenInclusive(random, min, max);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + String.valueOf(this.minInclusive) + "-" + String.valueOf(this.maxInclusive) + "]";
    }
}


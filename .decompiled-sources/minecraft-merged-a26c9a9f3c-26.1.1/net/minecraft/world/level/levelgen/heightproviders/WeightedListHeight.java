/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;

public class WeightedListHeight
extends HeightProvider {
    public static final MapCodec<WeightedListHeight> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)WeightedList.nonEmptyCodec(HeightProvider.CODEC).fieldOf("distribution")).forGetter(c -> c.distribution)).apply((Applicative<WeightedListHeight, ?>)i, WeightedListHeight::new));
    private final WeightedList<HeightProvider> distribution;

    public WeightedListHeight(WeightedList<HeightProvider> distribution) {
        this.distribution = distribution;
    }

    @Override
    public int sample(RandomSource random, WorldGenerationContext heightAccessor) {
        return this.distribution.getRandomOrThrow(random).sample(random, heightAccessor);
    }

    @Override
    public HeightProviderType<?> getType() {
        return HeightProviderType.WEIGHTED_LIST;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;

public record RemoveBinomial(LevelBasedValue chance) implements EnchantmentValueEffect
{
    public static final MapCodec<RemoveBinomial> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)LevelBasedValue.CODEC.fieldOf("chance")).forGetter(RemoveBinomial::chance)).apply((Applicative<RemoveBinomial, ?>)i, RemoveBinomial::new));

    @Override
    public float process(int level, RandomSource random, float n) {
        float p = this.chance.calculate(level);
        int drop = 0;
        if (n <= 128.0f || n * p < 20.0f || n * (1.0f - p) < 20.0f) {
            int y = 0;
            while ((float)y < n) {
                if (random.nextFloat() < p) {
                    ++drop;
                }
                ++y;
            }
        } else {
            double miu = Math.floor(n * p);
            double sigma = Math.sqrt(n * p * (1.0f - p));
            drop = (int)Math.round(miu + random.nextGaussian() * sigma);
            drop = Math.clamp((long)drop, 0, (int)n);
        }
        return n - (float)drop;
    }

    public MapCodec<RemoveBinomial> codec() {
        return CODEC;
    }
}


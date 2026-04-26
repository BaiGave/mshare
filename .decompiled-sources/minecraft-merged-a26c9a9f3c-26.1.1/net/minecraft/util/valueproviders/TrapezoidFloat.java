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
import net.minecraft.util.valueproviders.FloatProvider;

public record TrapezoidFloat(float min, float max, float plateau) implements FloatProvider
{
    public static final MapCodec<TrapezoidFloat> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("min")).forGetter(TrapezoidFloat::min), ((MapCodec)Codec.FLOAT.fieldOf("max")).forGetter(TrapezoidFloat::max), ((MapCodec)Codec.FLOAT.fieldOf("plateau")).forGetter(TrapezoidFloat::plateau)).apply((Applicative<TrapezoidFloat, ?>)i, TrapezoidFloat::new)).validate(c -> {
        if (c.max < c.min) {
            return DataResult.error(() -> "Max must be larger than min: [" + c.min + ", " + c.max + "]");
        }
        if (c.plateau > c.max - c.min) {
            return DataResult.error(() -> "Plateau can at most be the full span: [" + c.min + ", " + c.max + "]");
        }
        return DataResult.success(c);
    });

    public static TrapezoidFloat of(float min, float max, float plateau) {
        return new TrapezoidFloat(min, max, plateau);
    }

    @Override
    public float sample(RandomSource random) {
        float range = this.max - this.min;
        float plateauStart = (range - this.plateau) / 2.0f;
        float plateauEnd = range - plateauStart;
        return this.min + random.nextFloat() * plateauEnd + random.nextFloat() * plateauStart;
    }

    public MapCodec<TrapezoidFloat> codec() {
        return MAP_CODEC;
    }

    @Override
    public String toString() {
        return "trapezoid(" + this.plateau + ") in [" + this.min + "-" + this.max + "]";
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;

public record ConstantFloat(float value) implements FloatProvider
{
    public static final ConstantFloat ZERO = new ConstantFloat(0.0f);
    public static final MapCodec<ConstantFloat> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("value")).forGetter(ConstantFloat::value)).apply((Applicative<ConstantFloat, ?>)i, ConstantFloat::of));

    public static ConstantFloat of(float value) {
        if (value == 0.0f) {
            return ZERO;
        }
        return new ConstantFloat(value);
    }

    @Override
    public float sample(RandomSource random) {
        return this.value;
    }

    @Override
    public float min() {
        return this.value;
    }

    @Override
    public float max() {
        return this.value;
    }

    public MapCodec<ConstantFloat> codec() {
        return MAP_CODEC;
    }

    @Override
    public String toString() {
        return Float.toString(this.value);
    }
}


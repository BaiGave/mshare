/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FloatWithAlpha(float value, float alpha) {
    private static final Codec<FloatWithAlpha> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("value")).forGetter(FloatWithAlpha::value), Codec.floatRange(0.0f, 1.0f).optionalFieldOf("alpha", Float.valueOf(1.0f)).forGetter(FloatWithAlpha::alpha)).apply((Applicative<FloatWithAlpha, ?>)i, FloatWithAlpha::new));
    public static final Codec<FloatWithAlpha> CODEC = Codec.either(Codec.FLOAT, FULL_CODEC).xmap(either -> either.map(FloatWithAlpha::new, p -> p), parameter -> parameter.alpha() == 1.0f ? Either.left(Float.valueOf(parameter.value())) : Either.right(parameter));

    public FloatWithAlpha(float value) {
        this(value, 1.0f);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.particles;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;

public class SpellParticleOption
implements ParticleOptions {
    private final ParticleType<SpellParticleOption> type;
    private final int color;
    private final float power;

    public static MapCodec<SpellParticleOption> codec(ParticleType<SpellParticleOption> type) {
        return RecordCodecBuilder.mapCodec(i -> i.group(ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color", -1).forGetter(o -> o.color), Codec.FLOAT.optionalFieldOf("power", Float.valueOf(1.0f)).forGetter(o -> Float.valueOf(o.power))).apply((Applicative<SpellParticleOption, ?>)i, (color, power) -> new SpellParticleOption(type, (int)color, power.floatValue())));
    }

    public static StreamCodec<? super ByteBuf, SpellParticleOption> streamCodec(ParticleType<SpellParticleOption> type) {
        return StreamCodec.composite(ByteBufCodecs.INT, o -> o.color, ByteBufCodecs.FLOAT, o -> Float.valueOf(o.power), (color, power) -> new SpellParticleOption(type, (int)color, power.floatValue()));
    }

    private SpellParticleOption(ParticleType<SpellParticleOption> type, int color, float power) {
        this.type = type;
        this.color = color;
        this.power = power;
    }

    public ParticleType<SpellParticleOption> getType() {
        return this.type;
    }

    public float getRed() {
        return (float)ARGB.red(this.color) / 255.0f;
    }

    public float getGreen() {
        return (float)ARGB.green(this.color) / 255.0f;
    }

    public float getBlue() {
        return (float)ARGB.blue(this.color) / 255.0f;
    }

    public float getPower() {
        return this.power;
    }

    public static SpellParticleOption create(ParticleType<SpellParticleOption> type, int color, float power) {
        return new SpellParticleOption(type, color, power);
    }

    public static SpellParticleOption create(ParticleType<SpellParticleOption> type, float red, float green, float blue, float power) {
        return SpellParticleOption.create(type, ARGB.colorFromFloat(1.0f, red, green, blue), power);
    }
}


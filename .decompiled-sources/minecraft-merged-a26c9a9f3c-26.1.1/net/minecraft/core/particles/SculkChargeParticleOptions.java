/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.particles;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SculkChargeParticleOptions(float roll) implements ParticleOptions
{
    public static final MapCodec<SculkChargeParticleOptions> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("roll")).forGetter(o -> Float.valueOf(o.roll))).apply((Applicative<SculkChargeParticleOptions, ?>)i, SculkChargeParticleOptions::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SculkChargeParticleOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, o -> Float.valueOf(o.roll), SculkChargeParticleOptions::new);

    public ParticleType<SculkChargeParticleOptions> getType() {
        return ParticleTypes.SCULK_CHARGE;
    }
}


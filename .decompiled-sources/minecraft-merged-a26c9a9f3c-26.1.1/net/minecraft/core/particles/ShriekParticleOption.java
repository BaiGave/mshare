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

public class ShriekParticleOption
implements ParticleOptions {
    public static final MapCodec<ShriekParticleOption> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.INT.fieldOf("delay")).forGetter(o -> o.delay)).apply((Applicative<ShriekParticleOption, ?>)i, ShriekParticleOption::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShriekParticleOption> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, o -> o.delay, ShriekParticleOption::new);
    private final int delay;

    public ShriekParticleOption(int delay) {
        this.delay = delay;
    }

    public ParticleType<ShriekParticleOption> getType() {
        return ParticleTypes.SHRIEK;
    }

    public int getDelay() {
        return this.delay;
    }
}


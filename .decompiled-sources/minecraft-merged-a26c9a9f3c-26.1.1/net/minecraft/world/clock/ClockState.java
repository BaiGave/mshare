/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.clock;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record ClockState(long totalTicks, float partialTick, float rate, boolean paused) {
    public static final Codec<ClockState> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.LONG.fieldOf("total_ticks")).forGetter(ClockState::totalTicks), Codec.FLOAT.optionalFieldOf("partial_tick", Float.valueOf(0.0f)).forGetter(ClockState::partialTick), ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("rate", Float.valueOf(1.0f)).forGetter(ClockState::rate), Codec.BOOL.optionalFieldOf("paused", false).forGetter(ClockState::paused)).apply((Applicative<ClockState, ?>)i, ClockState::new));
}


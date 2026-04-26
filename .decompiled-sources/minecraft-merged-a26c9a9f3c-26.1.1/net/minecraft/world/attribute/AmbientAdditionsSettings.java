/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.attribute;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public record AmbientAdditionsSettings(Holder<SoundEvent> soundEvent, double tickChance) {
    public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)SoundEvent.CODEC.fieldOf("sound")).forGetter(s -> s.soundEvent), ((MapCodec)Codec.DOUBLE.fieldOf("tick_chance")).forGetter(s -> s.tickChance)).apply((Applicative<AmbientAdditionsSettings, ?>)i, AmbientAdditionsSettings::new));
}


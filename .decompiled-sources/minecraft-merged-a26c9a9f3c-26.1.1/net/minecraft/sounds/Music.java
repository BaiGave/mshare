/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.sounds;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;

public record Music(Holder<SoundEvent> sound, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
    public static final Codec<Music> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)SoundEvent.CODEC.fieldOf("sound")).forGetter(Music::sound), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("min_delay")).forGetter(Music::minDelay), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_delay")).forGetter(Music::maxDelay), Codec.BOOL.optionalFieldOf("replace_current_music", false).forGetter(Music::replaceCurrentMusic)).apply((Applicative<Music, ?>)i, Music::new));
}


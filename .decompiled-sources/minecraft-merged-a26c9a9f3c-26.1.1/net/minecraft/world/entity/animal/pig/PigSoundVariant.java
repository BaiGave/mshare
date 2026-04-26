/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.pig;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.sounds.SoundEvent;

public record PigSoundVariant(PigSoundSet adultSounds, PigSoundSet babySounds) {
    public static final Codec<PigSoundVariant> DIRECT_CODEC = PigSoundVariant.codec();
    public static final Codec<PigSoundVariant> NETWORK_CODEC = PigSoundVariant.codec();
    public static final Codec<Holder<PigSoundVariant>> CODEC = RegistryFixedCodec.create(Registries.PIG_SOUND_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<PigSoundVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.PIG_SOUND_VARIANT);

    private static Codec<PigSoundVariant> codec() {
        return RecordCodecBuilder.create(i -> i.group(((MapCodec)PigSoundSet.CODEC.fieldOf("adult_sounds")).forGetter(PigSoundVariant::adultSounds), ((MapCodec)PigSoundSet.CODEC.fieldOf("baby_sounds")).forGetter(PigSoundVariant::babySounds)).apply((Applicative<PigSoundVariant, ?>)i, PigSoundVariant::new));
    }

    public record PigSoundSet(Holder<SoundEvent> ambientSound, Holder<SoundEvent> hurtSound, Holder<SoundEvent> deathSound, Holder<SoundEvent> stepSound, Holder<SoundEvent> eatSound) {
        public static final Codec<PigSoundSet> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)SoundEvent.CODEC.fieldOf("ambient_sound")).forGetter(PigSoundSet::ambientSound), ((MapCodec)SoundEvent.CODEC.fieldOf("hurt_sound")).forGetter(PigSoundSet::hurtSound), ((MapCodec)SoundEvent.CODEC.fieldOf("death_sound")).forGetter(PigSoundSet::deathSound), ((MapCodec)SoundEvent.CODEC.fieldOf("step_sound")).forGetter(PigSoundSet::stepSound), ((MapCodec)SoundEvent.CODEC.fieldOf("eat_sound")).forGetter(PigSoundSet::eatSound)).apply((Applicative<PigSoundSet, ?>)i, PigSoundSet::new));
    }
}


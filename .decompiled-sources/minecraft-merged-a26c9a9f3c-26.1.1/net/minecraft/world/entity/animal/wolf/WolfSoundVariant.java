/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.wolf;

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

public record WolfSoundVariant(WolfSoundSet adultSounds, WolfSoundSet babySounds) {
    public static final Codec<WolfSoundVariant> DIRECT_CODEC = WolfSoundVariant.getWolfSoundVariantCodec();
    public static final Codec<WolfSoundVariant> NETWORK_CODEC = WolfSoundVariant.getWolfSoundVariantCodec();
    public static final Codec<Holder<WolfSoundVariant>> CODEC = RegistryFixedCodec.create(Registries.WOLF_SOUND_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfSoundVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WOLF_SOUND_VARIANT);

    private static Codec<WolfSoundVariant> getWolfSoundVariantCodec() {
        return RecordCodecBuilder.create(i -> i.group(((MapCodec)WolfSoundSet.CODEC.fieldOf("adult_sounds")).forGetter(WolfSoundVariant::adultSounds), ((MapCodec)WolfSoundSet.CODEC.fieldOf("baby_sounds")).forGetter(WolfSoundVariant::babySounds)).apply((Applicative<WolfSoundVariant, ?>)i, WolfSoundVariant::new));
    }

    public record WolfSoundSet(Holder<SoundEvent> ambientSound, Holder<SoundEvent> deathSound, Holder<SoundEvent> growlSound, Holder<SoundEvent> hurtSound, Holder<SoundEvent> pantSound, Holder<SoundEvent> whineSound, Holder<SoundEvent> stepSound) {
        public static final Codec<WolfSoundSet> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)SoundEvent.CODEC.fieldOf("ambient_sound")).forGetter(WolfSoundSet::ambientSound), ((MapCodec)SoundEvent.CODEC.fieldOf("death_sound")).forGetter(WolfSoundSet::deathSound), ((MapCodec)SoundEvent.CODEC.fieldOf("growl_sound")).forGetter(WolfSoundSet::growlSound), ((MapCodec)SoundEvent.CODEC.fieldOf("hurt_sound")).forGetter(WolfSoundSet::hurtSound), ((MapCodec)SoundEvent.CODEC.fieldOf("pant_sound")).forGetter(WolfSoundSet::pantSound), ((MapCodec)SoundEvent.CODEC.fieldOf("whine_sound")).forGetter(WolfSoundSet::whineSound), ((MapCodec)SoundEvent.CODEC.fieldOf("step_sound")).forGetter(WolfSoundSet::stepSound)).apply((Applicative<WolfSoundSet, ?>)i, WolfSoundSet::new));
    }
}


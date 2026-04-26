/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.cow;

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

public record CowSoundVariant(Holder<SoundEvent> ambientSound, Holder<SoundEvent> hurtSound, Holder<SoundEvent> deathSound, Holder<SoundEvent> stepSound) {
    public static final Codec<CowSoundVariant> DIRECT_CODEC = CowSoundVariant.codec();
    public static final Codec<CowSoundVariant> NETWORK_CODEC = CowSoundVariant.codec();
    public static final Codec<Holder<CowSoundVariant>> CODEC = RegistryFixedCodec.create(Registries.COW_SOUND_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<CowSoundVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.COW_SOUND_VARIANT);

    private static Codec<CowSoundVariant> codec() {
        return RecordCodecBuilder.create(i -> i.group(((MapCodec)SoundEvent.CODEC.fieldOf("ambient_sound")).forGetter(CowSoundVariant::ambientSound), ((MapCodec)SoundEvent.CODEC.fieldOf("hurt_sound")).forGetter(CowSoundVariant::hurtSound), ((MapCodec)SoundEvent.CODEC.fieldOf("death_sound")).forGetter(CowSoundVariant::deathSound), ((MapCodec)SoundEvent.CODEC.fieldOf("step_sound")).forGetter(CowSoundVariant::stepSound)).apply((Applicative<CowSoundVariant, ?>)i, CowSoundVariant::new));
    }
}


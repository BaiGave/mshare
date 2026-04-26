/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;

public record JukeboxSong(Holder<SoundEvent> soundEvent, Component description, float lengthInSeconds, int comparatorOutput) {
    public static final Codec<JukeboxSong> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)SoundEvent.CODEC.fieldOf("sound_event")).forGetter(JukeboxSong::soundEvent), ((MapCodec)ComponentSerialization.CODEC.fieldOf("description")).forGetter(JukeboxSong::description), ((MapCodec)ExtraCodecs.POSITIVE_FLOAT.fieldOf("length_in_seconds")).forGetter(JukeboxSong::lengthInSeconds), ((MapCodec)ExtraCodecs.intRange(0, 15).fieldOf("comparator_output")).forGetter(JukeboxSong::comparatorOutput)).apply((Applicative<JukeboxSong, ?>)i, JukeboxSong::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxSong> DIRECT_STREAM_CODEC = StreamCodec.composite(SoundEvent.STREAM_CODEC, JukeboxSong::soundEvent, ComponentSerialization.STREAM_CODEC, JukeboxSong::description, ByteBufCodecs.FLOAT, JukeboxSong::lengthInSeconds, ByteBufCodecs.VAR_INT, JukeboxSong::comparatorOutput, JukeboxSong::new);
    public static final Codec<Holder<JukeboxSong>> CODEC = RegistryFixedCodec.create(Registries.JUKEBOX_SONG);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<JukeboxSong>> STREAM_CODEC = ByteBufCodecs.holder(Registries.JUKEBOX_SONG, DIRECT_STREAM_CODEC);
    private static final int SONG_END_PADDING_TICKS = 20;

    public int lengthInTicks() {
        return Mth.ceil(this.lengthInSeconds * 20.0f);
    }

    public boolean hasFinished(long ticksElapsed) {
        return ticksElapsed >= (long)(this.lengthInTicks() + 20);
    }

    public static Optional<Holder<JukeboxSong>> fromStack(ItemStack stack) {
        JukeboxPlayable jukeboxPlayable = stack.get(DataComponents.JUKEBOX_PLAYABLE);
        if (jukeboxPlayable != null) {
            return Optional.of(jukeboxPlayable.song());
        }
        return Optional.empty();
    }
}


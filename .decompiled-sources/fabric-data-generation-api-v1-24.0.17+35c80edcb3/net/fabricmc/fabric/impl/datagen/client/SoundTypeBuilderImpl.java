/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.datagen.client;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.client.datagen.v1.builder.SoundTypeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.common.base.Preconditions;

public final class SoundTypeBuilderImpl
implements SoundTypeBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoundTypeBuilderImpl.class);
    private boolean replace = false;
    private @Nullable String subtitle;
    private final List<Entry> sounds = new ArrayList<Entry>();

    @Override
    public SoundTypeBuilder replace(boolean replace) {
        this.replace = replace;
        return this;
    }

    @Override
    public SoundTypeBuilder subtitle(@Nullable String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    @Override
    public SoundTypeBuilder sound(SoundTypeBuilder.RegistrationBuilder sound) {
        Objects.requireNonNull(sound, "Sound must not be null.");
        this.sounds.add(((RegistrationBuilderImpl)sound).build(""));
        return this;
    }

    @Override
    public SoundTypeBuilder sound(SoundTypeBuilder.RegistrationBuilder sound, int count) {
        Objects.requireNonNull(sound, "Sound must not be null.");
        Preconditions.checkArgument(count > 0, "Count must be greater than zero.");
        for (int i = 1; i <= count; ++i) {
            this.sounds.add(((RegistrationBuilderImpl)sound).build(Integer.toString(i)));
        }
        return this;
    }

    public SoundType build() {
        Preconditions.checkState(!this.sounds.isEmpty(), "Sound definition must have at least one sound file");
        for (Entry sound : this.sounds) {
            if (sound.type() != SoundTypeBuilder.RegistrationType.SOUND_EVENT) continue;
            BuiltInRegistries.SOUND_EVENT.getOptional(sound.name()).orElseThrow(() -> new IllegalStateException("Referenced sound event " + String.valueOf(sound.name()) + " does not exist"));
        }
        return new SoundType(this.sounds, this.replace, Optional.ofNullable(this.subtitle));
    }

    public static final class RegistrationBuilderImpl
    implements SoundTypeBuilder.RegistrationBuilder {
        private final Identifier id;
        private final SoundTypeBuilder.RegistrationType type;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private int attenuationDistance = 16;
        private int weight = 1;
        private boolean stream = false;
        private boolean preload = false;

        private RegistrationBuilderImpl(SoundTypeBuilder.RegistrationType type, Identifier id) {
            this.type = type;
            this.id = id;
        }

        public static SoundTypeBuilder.RegistrationBuilder create(SoundTypeBuilder.RegistrationType type, Identifier id) {
            return new RegistrationBuilderImpl(type, id);
        }

        public static SoundTypeBuilder.RegistrationBuilder ofFile(Identifier soundFile) {
            Objects.requireNonNull(soundFile, "Sound file/event id must not be null.");
            if (soundFile.getPath().indexOf(46) != -1) {
                LOGGER.warn("Sound file \"" + String.valueOf(soundFile) + "\" should not have a file extension and may result in the sound event not playing.");
            }
            return RegistrationBuilderImpl.create(SoundTypeBuilder.RegistrationType.FILE, soundFile);
        }

        public static SoundTypeBuilder.RegistrationBuilder ofEvent(SoundEvent event) {
            Objects.requireNonNull(event, "Sound event must not be null.");
            return RegistrationBuilderImpl.create(SoundTypeBuilder.RegistrationType.SOUND_EVENT, event.location());
        }

        public static SoundTypeBuilder.RegistrationBuilder ofEvent(Holder<SoundEvent> event) {
            Objects.requireNonNull(event, "Sound event key must not be null.");
            return RegistrationBuilderImpl.create(SoundTypeBuilder.RegistrationType.SOUND_EVENT, event.unwrapKey().orElseThrow(() -> new IllegalArgumentException("Direct (non-registered) sound event cannot be added")).identifier());
        }

        @Override
        public SoundTypeBuilder.RegistrationBuilder volume(float volume) {
            Preconditions.checkArgument(volume > 0.0f && volume <= 1.0f, "Sound volume must be greater than 0 and less than or equal to 1.");
            this.volume = volume;
            return this;
        }

        @Override
        public SoundTypeBuilder.RegistrationBuilder pitch(float pitch) {
            Preconditions.checkArgument(pitch >= 0.5f && pitch <= 2.0f, "Sound pitch must be between 0.5 and 2 (inclusive)");
            this.pitch = pitch;
            return this;
        }

        @Override
        public SoundTypeBuilder.RegistrationBuilder attenuationDistance(int attenuationDistance) {
            this.attenuationDistance = attenuationDistance;
            return this;
        }

        @Override
        public SoundTypeBuilder.RegistrationBuilder weight(int weight) {
            Preconditions.checkArgument(weight >= 1, "Sound must have a weight of at least 1.");
            this.weight = weight;
            return this;
        }

        @Override
        public SoundTypeBuilder.RegistrationBuilder stream(boolean stream) {
            this.stream = stream;
            return this;
        }

        @Override
        public SoundTypeBuilder.RegistrationBuilder preload(boolean preload) {
            this.preload = preload;
            return this;
        }

        public Entry build(@Nullable String suffix) {
            return new Entry(this.id.withSuffix(suffix == null ? "" : suffix), this.type, this.volume, this.pitch, this.weight, this.attenuationDistance, this.stream, this.preload);
        }
    }

    public record Entry(Identifier name, SoundTypeBuilder.RegistrationType type, float volume, float pitch, int weight, int attenuationDistance, boolean stream, boolean preload) {
        private static final Codec<Entry> MAP_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Identifier.CODEC.fieldOf("name")).forGetter(Entry::name), SoundTypeBuilder.RegistrationType.CODEC.optionalFieldOf("type", SoundTypeBuilder.RegistrationType.FILE).forGetter(Entry::type), Codec.floatRange(Float.MIN_VALUE, 1.0f).optionalFieldOf("volume", Float.valueOf(1.0f)).forGetter(Entry::volume), Codec.floatRange(0.5f, 2.0f).optionalFieldOf("pitch", Float.valueOf(1.0f)).forGetter(Entry::pitch), Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("weight", 1).forGetter(Entry::weight), Codec.INT.optionalFieldOf("attenuation_distance", 16).forGetter(Entry::attenuationDistance), Codec.BOOL.optionalFieldOf("stream", false).forGetter(Entry::stream), Codec.BOOL.optionalFieldOf("preload", false).forGetter(Entry::preload)).apply((Applicative<Entry, ?>)instance, Entry::new));
        private static final Codec<Entry> STRING_CODEC = Identifier.CODEC.xmap(id -> new Entry((Identifier)id, SoundTypeBuilder.RegistrationType.FILE, 1.0f, 1.0f, 1, 16, false, false), Entry::name);
        private static final Codec<Entry> CODEC = Codec.xor(STRING_CODEC, MAP_CODEC).xmap(Either::unwrap, sound -> {
            if (sound.type() != SoundTypeBuilder.RegistrationType.FILE || sound.volume() != 1.0f || sound.pitch() != 1.0f || sound.weight() != 1 || sound.attenuationDistance() != 16 || sound.stream() || sound.preload()) {
                return Either.right(sound);
            }
            return Either.left(sound);
        });
    }

    public record SoundType(List<Entry> sounds, boolean replace, Optional<String> subtitle) {
        public static final Codec<SoundType> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Entry.CODEC.listOf().fieldOf("sounds")).forGetter(SoundType::sounds), Codec.BOOL.optionalFieldOf("replace", false).forGetter(SoundType::replace), Codec.STRING.optionalFieldOf("subtitle").forGetter(SoundType::subtitle)).apply((Applicative<SoundType, ?>)instance, SoundType::new));
    }
}


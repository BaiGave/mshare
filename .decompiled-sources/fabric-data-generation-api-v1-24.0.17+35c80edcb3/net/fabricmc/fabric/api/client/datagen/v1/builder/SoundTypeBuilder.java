/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.datagen.v1.builder;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.fabricmc.fabric.impl.datagen.client.SoundTypeBuilderImpl;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface SoundTypeBuilder {
    public static SoundTypeBuilder of(SoundEvent event) {
        Objects.requireNonNull(event, "Sound event cannot be null.");
        return SoundTypeBuilder.of().subtitle(Util.makeDescriptionId("subtitles", event.location()));
    }

    public static SoundTypeBuilder of() {
        return new SoundTypeBuilderImpl();
    }

    @Deprecated(forRemoval=true)
    default public SoundTypeBuilder source(SoundSource source) {
        return this;
    }

    public SoundTypeBuilder replace(boolean var1);

    public SoundTypeBuilder subtitle(@Nullable String var1);

    public SoundTypeBuilder sound(RegistrationBuilder var1);

    public SoundTypeBuilder sound(RegistrationBuilder var1, int var2);

    @ApiStatus.NonExtendable
    public static interface RegistrationBuilder {
        public static final float DEFAULT_VOLUME = 1.0f;
        public static final float DEFAULT_PITCH = 1.0f;
        public static final int DEFAULT_WEIGHT = 1;
        public static final int DEFAULT_ATTENUATION_DISTANCE = 16;

        public static RegistrationBuilder create(RegistrationType type, Identifier id) {
            return SoundTypeBuilderImpl.RegistrationBuilderImpl.create(type, id);
        }

        public static RegistrationBuilder ofFile(Identifier soundFile) {
            return SoundTypeBuilderImpl.RegistrationBuilderImpl.ofFile(soundFile);
        }

        public static RegistrationBuilder ofEvent(SoundEvent event) {
            return SoundTypeBuilderImpl.RegistrationBuilderImpl.ofEvent(event);
        }

        public static RegistrationBuilder ofEvent(Holder<SoundEvent> event) {
            return SoundTypeBuilderImpl.RegistrationBuilderImpl.ofEvent(event);
        }

        public RegistrationBuilder volume(float var1);

        public RegistrationBuilder pitch(float var1);

        public RegistrationBuilder attenuationDistance(int var1);

        public RegistrationBuilder weight(int var1);

        public RegistrationBuilder stream(boolean var1);

        public RegistrationBuilder preload(boolean var1);
    }

    public static enum RegistrationType implements StringRepresentable
    {
        FILE("file"),
        SOUND_EVENT("event");

        public static final Codec<RegistrationType> CODEC;
        private final String name;

        private RegistrationType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(RegistrationType::values);
        }
    }
}


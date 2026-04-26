/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.particle.v1;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class FabricParticleTypes {
    private FabricParticleTypes() {
    }

    public static SimpleParticleType simple() {
        return FabricParticleTypes.simple(false);
    }

    public static SimpleParticleType simple(boolean alwaysSpawn) {
        return new SimpleParticleType(alwaysSpawn){};
    }

    public static <T extends ParticleOptions> ParticleType<T> complex(MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return FabricParticleTypes.complex(false, codec, streamCodec);
    }

    public static <T extends ParticleOptions> ParticleType<T> complex(boolean alwaysSpawn, final MapCodec<T> codec, final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return new ParticleType<T>(alwaysSpawn){

            @Override
            public MapCodec<T> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodec;
            }
        };
    }

    public static <T extends ParticleOptions> ParticleType<T> complex(Function<ParticleType<T>, MapCodec<T>> codecGetter, Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodecGetter) {
        return FabricParticleTypes.complex(false, codecGetter, streamCodecGetter);
    }

    public static <T extends ParticleOptions> ParticleType<T> complex(boolean alwaysSpawn, final Function<ParticleType<T>, MapCodec<T>> codecGetter, final Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodecGetter) {
        return new ParticleType<T>(alwaysSpawn){

            @Override
            public MapCodec<T> codec() {
                return (MapCodec)codecGetter.apply(this);
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return (StreamCodec)streamCodecGetter.apply(this);
            }
        };
    }
}


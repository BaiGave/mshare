/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.particle.v1;

import java.util.Locale;
import java.util.function.Function;
import net.fabricmc.fabric.impl.client.particle.ParticleGroupRegistryImpl;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class ParticleGroupRegistry {
    public static void register(ParticleRenderType renderType, Function<ParticleEngine, ParticleGroup<?>> function) {
        ParticleGroupRegistryImpl.INSTANCE.register(renderType, function);
    }

    public static void registerOrdering(ParticleRenderType first, Identifier second) {
        ParticleGroupRegistry.registerOrdering(ParticleGroupRegistry.getId(first), second);
    }

    public static void registerOrdering(ParticleRenderType first, ParticleRenderType second) {
        ParticleGroupRegistry.registerOrdering(ParticleGroupRegistry.getId(first), ParticleGroupRegistry.getId(second));
    }

    public static void registerOrdering(Identifier first, ParticleRenderType second) {
        ParticleGroupRegistry.registerOrdering(first, ParticleGroupRegistry.getId(second));
    }

    public static void registerOrdering(Identifier first, Identifier second) {
        ParticleGroupRegistryImpl.INSTANCE.registerOrdering(first, second);
    }

    public static @Nullable ParticleRenderType getParticleRenderType(Identifier id) {
        return ParticleGroupRegistryImpl.INSTANCE.getParticleRenderType(id);
    }

    public static Identifier getId(ParticleRenderType renderType) {
        if (renderType == ParticleRenderType.SINGLE_QUADS || renderType == ParticleRenderType.NO_RENDER || renderType == ParticleRenderType.ELDER_GUARDIANS || renderType == ParticleRenderType.ITEM_PICKUP) {
            return Identifier.withDefaultNamespace(renderType.name().toLowerCase(Locale.ROOT));
        }
        return Identifier.parse(renderType.name());
    }

    private ParticleGroupRegistry() {
    }
}


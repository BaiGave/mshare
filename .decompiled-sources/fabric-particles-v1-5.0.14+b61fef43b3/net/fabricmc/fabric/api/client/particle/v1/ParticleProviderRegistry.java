/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.particle.v1;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.fabricmc.fabric.impl.client.particle.ParticleProviderRegistryImpl;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ParticleProviderRegistry {
    public static ParticleProviderRegistry getInstance() {
        return ParticleProviderRegistryImpl.INSTANCE;
    }

    public <T extends ParticleOptions> void register(ParticleType<T> var1, ParticleProvider<T> var2);

    public <T extends ParticleOptions> void register(ParticleType<T> var1, PendingParticleProvider<T> var2);

    @FunctionalInterface
    public static interface PendingParticleProvider<T extends ParticleOptions> {
        public ParticleProvider<T> create(FabricSpriteSet var1);
    }
}


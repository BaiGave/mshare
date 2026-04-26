/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.particle;

import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.impl.client.particle.FabricSpriteSetImpl;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ParticleProviderRegistryImpl
implements ParticleProviderRegistry {
    public static final ParticleProviderRegistryImpl INSTANCE = new ParticleProviderRegistryImpl();
    ParticleProviderRegistry internalRegistry = new DeferredParticleProviderRegistry();

    private ParticleProviderRegistryImpl() {
    }

    @Override
    public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
        this.internalRegistry.register(type, provider);
    }

    @Override
    public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProviderRegistry.PendingParticleProvider<T> constructor) {
        this.internalRegistry.register(type, constructor);
    }

    public void initialize(ParticleResources particleResources) {
        DirectParticleProviderRegistry newRegistry = new DirectParticleProviderRegistry(particleResources);
        DeferredParticleProviderRegistry oldRegistry = (DeferredParticleProviderRegistry)this.internalRegistry;
        oldRegistry.applyTo(newRegistry);
        this.internalRegistry = newRegistry;
    }

    static class DeferredParticleProviderRegistry
    implements ParticleProviderRegistry {
        private final Map<ParticleType<?>, ParticleProvider<?>> factories = new IdentityHashMap();
        private final Map<ParticleType<?>, ParticleProviderRegistry.PendingParticleProvider<?>> constructors = new IdentityHashMap();

        DeferredParticleProviderRegistry() {
        }

        @Override
        public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
            this.factories.put(type, provider);
        }

        @Override
        public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProviderRegistry.PendingParticleProvider<T> factory) {
            this.constructors.put(type, factory);
        }

        void applyTo(ParticleProviderRegistry registry) {
            ParticleType<?> type;
            for (Map.Entry<ParticleType<?>, ParticleProvider<?>> entry : this.factories.entrySet()) {
                type = entry.getKey();
                ParticleProvider<?> factory = entry.getValue();
                registry.register(type, factory);
            }
            for (Map.Entry<ParticleType<?>, Object> entry : this.constructors.entrySet()) {
                type = entry.getKey();
                ParticleProviderRegistry.PendingParticleProvider constructor = (ParticleProviderRegistry.PendingParticleProvider)entry.getValue();
                registry.register(type, constructor);
            }
        }
    }

    record DirectParticleProviderRegistry(ParticleResources particleResources) implements ParticleProviderRegistry
    {
        @Override
        public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider) {
            this.particleResources.providers.put(BuiltInRegistries.PARTICLE_TYPE.getId(type), (ParticleProvider<?>)provider);
        }

        @Override
        public <T extends ParticleOptions> void register(ParticleType<T> type, ParticleProviderRegistry.PendingParticleProvider<T> constructor) {
            ParticleResources.MutableSpriteSet delegate = new ParticleResources.MutableSpriteSet();
            FabricSpriteSetImpl fabricSpriteSet = new FabricSpriteSetImpl(delegate);
            this.particleResources.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(type), delegate);
            this.register(type, constructor.create(fabricSpriteSet));
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.impl.registry.sync.trackers.Int2ObjectMapTracker;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ParticleResources.class})
public class ParticleResourcesMixin {
    @Final
    @Shadow
    private Int2ObjectMap<ParticleProvider<?>> providers;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void onInit(CallbackInfo info) {
        Int2ObjectMapTracker.register(BuiltInRegistries.PARTICLE_TYPE, "ParticleEngine.providers", this.providers);
    }
}


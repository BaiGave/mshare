/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.particle;

import net.fabricmc.fabric.impl.client.particle.ParticleProviderRegistryImpl;
import net.minecraft.client.particle.ParticleResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ParticleResources.class})
public abstract class ParticleResourcesMixin {
    @Inject(method={"registerProviders"}, at={@At(value="RETURN")})
    private void onRegisterDefaultFactories(CallbackInfo info) {
        ParticleProviderRegistryImpl.INSTANCE.initialize((ParticleResources)((Object)this));
    }
}


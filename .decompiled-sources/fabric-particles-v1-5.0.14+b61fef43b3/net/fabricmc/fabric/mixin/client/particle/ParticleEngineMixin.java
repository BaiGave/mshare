/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.impl.client.particle.ParticleGroupRegistryImpl;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ParticleEngine.class})
public abstract class ParticleEngineMixin {
    @Shadow
    @Final
    @Mutable
    private static List<ParticleRenderType> RENDER_ORDER;

    @Inject(method={"<clinit>"}, at={@At(value="RETURN")})
    private static void classInit(CallbackInfo ci) {
        RENDER_ORDER = new ArrayList<ParticleRenderType>(RENDER_ORDER);
    }

    @Inject(method={"createParticleGroup"}, at={@At(value="NEW", target="(Lnet/minecraft/client/particle/ParticleEngine;Lnet/minecraft/client/particle/ParticleRenderType;)Lnet/minecraft/client/particle/QuadParticleGroup;")}, cancellable=true)
    private void createParticleGroup(ParticleRenderType type, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        Function<ParticleEngine, ParticleGroup<?>> factory = ParticleGroupRegistryImpl.INSTANCE.getFactory(type);
        if (factory != null) {
            cir.setReturnValue(factory.apply((ParticleEngine)((Object)this)));
        }
    }
}


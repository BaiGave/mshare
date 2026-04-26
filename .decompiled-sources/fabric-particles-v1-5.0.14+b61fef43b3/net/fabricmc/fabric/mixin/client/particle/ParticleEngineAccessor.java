/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.particle;

import java.util.List;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ParticleEngine.class})
public interface ParticleEngineAccessor {
    @Accessor(value="RENDER_ORDER")
    public static List<ParticleRenderType> getParticleRenderTypes() {
        throw new IllegalStateException();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.state.level;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

@Environment(value=EnvType.CLIENT)
public class ParticlesRenderState
implements FabricRenderState {
    public final List<ParticleGroupRenderState> particles = new ArrayList<ParticleGroupRenderState>();

    public void reset() {
        this.particles.forEach(ParticleGroupRenderState::clear);
        this.particles.clear();
    }

    public void add(ParticleGroupRenderState state) {
        this.particles.add(state);
    }

    public void submit(SubmitNodeStorage submitNodeStorage, CameraRenderState camera) {
        for (ParticleGroupRenderState particle : this.particles) {
            particle.submit(submitNodeStorage, camera);
        }
    }
}


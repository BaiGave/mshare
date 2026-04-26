/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import com.google.common.collect.EvictingQueue;
import java.util.Iterator;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.core.particles.ParticleLimit;

@Environment(value=EnvType.CLIENT)
public abstract class ParticleGroup<P extends Particle> {
    private static final int MAX_PARTICLES = 16384;
    protected final ParticleEngine engine;
    protected final Queue<P> particles = EvictingQueue.create(16384);

    public ParticleGroup(ParticleEngine engine) {
        this.engine = engine;
    }

    public boolean isEmpty() {
        return this.particles.isEmpty();
    }

    public void tickParticles() {
        if (!this.particles.isEmpty()) {
            Iterator iterator = this.particles.iterator();
            while (iterator.hasNext()) {
                Particle particle = (Particle)iterator.next();
                this.tickParticle(particle);
                if (particle.isAlive()) continue;
                particle.getParticleLimit().ifPresent(options -> this.engine.updateCount((ParticleLimit)options, -1));
                iterator.remove();
            }
        }
    }

    private void tickParticle(Particle particle) {
        try {
            particle.tick();
        }
        catch (Throwable t) {
            CrashReport report = CrashReport.forThrowable(t, "Ticking Particle");
            CrashReportCategory category = report.addCategory("Particle being ticked");
            category.setDetail("Particle", particle::toString);
            category.setDetail("Particle Type", particle.getGroup()::toString);
            throw new ReportedException(report);
        }
    }

    public void add(Particle particle) {
        this.particles.add(particle);
    }

    public int size() {
        return this.particles.size();
    }

    public abstract ParticleGroupRenderState extractRenderState(Frustum var1, Camera var2, float var3);

    public Queue<P> getAll() {
        return this.particles;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.particle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class ParticleRenderEvents {
    public static final Event<AllowTerrainParticleTint> ALLOW_TERRAIN_PARTICLE_TINT = EventFactory.createArrayBacked(AllowTerrainParticleTint.class, callbacks -> (state, level, pos) -> {
        for (AllowTerrainParticleTint callback : callbacks) {
            if (callback.allowTerrainParticleTint(state, level, pos)) continue;
            return false;
        }
        return true;
    });

    private ParticleRenderEvents() {
    }

    @FunctionalInterface
    public static interface AllowTerrainParticleTint {
        public boolean allowTerrainParticleTint(BlockState var1, ClientLevel var2, BlockPos var3);
    }
}


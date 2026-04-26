/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class EntityTrackingEvents {
    public static final Event<StartTracking> START_TRACKING = EventFactory.createArrayBacked(StartTracking.class, callbacks -> (trackedEntity, player) -> {
        for (StartTracking callback : callbacks) {
            callback.onStartTracking(trackedEntity, player);
        }
    });
    public static final Event<StopTracking> STOP_TRACKING = EventFactory.createArrayBacked(StopTracking.class, callbacks -> (trackedEntity, player) -> {
        for (StopTracking callback : callbacks) {
            callback.onStopTracking(trackedEntity, player);
        }
    });

    private EntityTrackingEvents() {
    }

    @FunctionalInterface
    public static interface StopTracking {
        public void onStopTracking(Entity var1, ServerPlayer var2);
    }

    @FunctionalInterface
    public static interface StartTracking {
        public void onStartTracking(Entity var1, ServerPlayer var2);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity.event.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

public final class EntityElytraEvents {
    public static final Event<Allow> ALLOW = EventFactory.createArrayBacked(Allow.class, listeners -> entity -> {
        for (Allow listener : listeners) {
            if (listener.allowElytraFlight(entity)) continue;
            return false;
        }
        return true;
    });
    public static final Event<Custom> CUSTOM = EventFactory.createArrayBacked(Custom.class, listeners -> (entity, tickElytra) -> {
        for (Custom listener : listeners) {
            if (!listener.useCustomElytra(entity, tickElytra)) continue;
            return true;
        }
        return false;
    });

    private EntityElytraEvents() {
    }

    @FunctionalInterface
    public static interface Custom {
        public boolean useCustomElytra(LivingEntity var1, boolean var2);
    }

    @FunctionalInterface
    public static interface Allow {
        public boolean allowElytraFlight(LivingEntity var1);
    }
}


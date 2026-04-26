/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public final class ClientEntityEvents {
    public static final Event<Load> ENTITY_LOAD = EventFactory.createArrayBacked(Load.class, callbacks -> (entity, level) -> {
        for (Load callback : callbacks) {
            callback.onLoad(entity, level);
        }
    });
    public static final Event<Unload> ENTITY_UNLOAD = EventFactory.createArrayBacked(Unload.class, callbacks -> (entity, level) -> {
        for (Unload callback : callbacks) {
            callback.onUnload(entity, level);
        }
    });

    private ClientEntityEvents() {
    }

    @FunctionalInterface
    public static interface Unload {
        public void onUnload(Entity var1, ClientLevel var2);
    }

    @FunctionalInterface
    public static interface Load {
        public void onLoad(Entity var1, ClientLevel var2);
    }
}


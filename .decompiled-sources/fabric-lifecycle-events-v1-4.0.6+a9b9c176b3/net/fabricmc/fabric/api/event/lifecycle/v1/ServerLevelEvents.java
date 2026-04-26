/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class ServerLevelEvents {
    public static final Event<Load> LOAD = EventFactory.createArrayBacked(Load.class, callbacks -> (server, level) -> {
        for (Load callback : callbacks) {
            callback.onLevelLoad(server, level);
        }
    });
    public static final Event<Unload> UNLOAD = EventFactory.createArrayBacked(Unload.class, callbacks -> (server, level) -> {
        for (Unload callback : callbacks) {
            callback.onLevelUnload(server, level);
        }
    });

    private ServerLevelEvents() {
    }

    @FunctionalInterface
    public static interface Unload {
        public void onLevelUnload(MinecraftServer var1, ServerLevel var2);
    }

    @FunctionalInterface
    public static interface Load {
        public void onLevelLoad(MinecraftServer var1, ServerLevel var2);
    }
}


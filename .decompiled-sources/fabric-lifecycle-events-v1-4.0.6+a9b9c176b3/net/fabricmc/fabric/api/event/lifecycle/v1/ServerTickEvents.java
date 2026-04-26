/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class ServerTickEvents {
    public static final Event<StartTick> START_SERVER_TICK = EventFactory.createArrayBacked(StartTick.class, callbacks -> server -> {
        for (StartTick event : callbacks) {
            event.onStartTick(server);
        }
    });
    public static final Event<EndTick> END_SERVER_TICK = EventFactory.createArrayBacked(EndTick.class, callbacks -> server -> {
        for (EndTick event : callbacks) {
            event.onEndTick(server);
        }
    });
    public static final Event<StartLevelTick> START_LEVEL_TICK = EventFactory.createArrayBacked(StartLevelTick.class, callbacks -> level -> {
        for (StartLevelTick callback : callbacks) {
            callback.onStartTick(level);
        }
    });
    public static final Event<EndLevelTick> END_LEVEL_TICK = EventFactory.createArrayBacked(EndLevelTick.class, callbacks -> level -> {
        for (EndLevelTick callback : callbacks) {
            callback.onEndTick(level);
        }
    });

    private ServerTickEvents() {
    }

    @FunctionalInterface
    public static interface EndLevelTick {
        public void onEndTick(ServerLevel var1);
    }

    @FunctionalInterface
    public static interface StartLevelTick {
        public void onStartTick(ServerLevel var1);
    }

    @FunctionalInterface
    public static interface EndTick {
        public void onEndTick(MinecraftServer var1);
    }

    @FunctionalInterface
    public static interface StartTick {
        public void onStartTick(MinecraftServer var1);
    }
}


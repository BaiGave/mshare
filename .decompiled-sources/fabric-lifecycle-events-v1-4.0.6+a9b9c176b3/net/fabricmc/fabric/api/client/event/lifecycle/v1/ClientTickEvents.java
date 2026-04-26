/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientTickEvents {
    public static final Event<StartTick> START_CLIENT_TICK = EventFactory.createArrayBacked(StartTick.class, callbacks -> client -> {
        for (StartTick event : callbacks) {
            event.onStartTick(client);
        }
    });
    public static final Event<EndTick> END_CLIENT_TICK = EventFactory.createArrayBacked(EndTick.class, callbacks -> client -> {
        for (EndTick event : callbacks) {
            event.onEndTick(client);
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

    private ClientTickEvents() {
    }

    @FunctionalInterface
    public static interface EndLevelTick {
        public void onEndTick(ClientLevel var1);
    }

    @FunctionalInterface
    public static interface StartLevelTick {
        public void onStartTick(ClientLevel var1);
    }

    @FunctionalInterface
    public static interface EndTick {
        public void onEndTick(Minecraft var1);
    }

    @FunctionalInterface
    public static interface StartTick {
        public void onStartTick(Minecraft var1);
    }
}


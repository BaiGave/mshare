/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gamerule;

import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.minecraft.world.level.gamerules.GameRule;
import org.jspecify.annotations.Nullable;

public final class GameRuleEventsImpl {
    private static final Map<GameRule<?>, Event<GameRuleEvents.ValueUpdate<?>>> VALUE_UPDATES = new IdentityHashMap();

    private GameRuleEventsImpl() {
    }

    public static <T> Event<GameRuleEvents.ValueUpdate<T>> changeCallback(GameRule<T> rule) {
        return VALUE_UPDATES.computeIfAbsent(rule, gameRule -> EventFactory.createArrayBacked(GameRuleEvents.ValueUpdate.class, callbacks -> (value, server) -> {
            for (GameRuleEvents.ValueUpdate changedCallback : callbacks) {
                changedCallback.onGameRuleUpdated(value, server);
            }
        }));
    }

    public static <T> @Nullable Event<GameRuleEvents.ValueUpdate<T>> getValueUpdate(GameRule<T> rule) {
        return VALUE_UPDATES.get(rule);
    }
}

